package com.vero.coreprocessor.ruleengine.service;

import com.vero.coreprocessor.components.*;
import com.vero.coreprocessor.constants.*;
import com.vero.coreprocessor.exceptions.*;
import com.vero.coreprocessor.institution.model.*;
import com.vero.coreprocessor.merchants.model.*;
import com.vero.coreprocessor.ruleengine.dtos.*;
import com.vero.coreprocessor.ruleengine.model.*;
import com.vero.coreprocessor.ruleengine.repository.*;
import com.vero.coreprocessor.terminals.model.*;
import com.vero.coreprocessor.transactions.enums.*;
import com.vero.coreprocessor.transactions.services.*;
import com.vero.coreprocessor.transactions.utilities.*;
import com.vero.coreprocessor.utils.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.jpos.iso.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.*;
import org.springframework.stereotype.*;

import java.math.*;
import java.util.*;

import static com.vero.coreprocessor.utils.Utils.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RuleEngineImpl implements RuleEngine, Constants {
    @Value("${defaultRoute}")
    String defaultDestination;
    private final ApplicationContext applicationContext;
    private final InstitutionRoutingRuleRepository institutionRoutingRuleRepository;
    private final InstitutionInternationalRuleRepository internationalRuleRepository;
    private final DefaultRoutingRuleRepository defaultRoutingRuleRepository;
    private final DefaultInternationalRuleRepository defaultInternationalRuleRepository;
    private final InternationalCardValidationService internationalCardValidationService;
    private final ProcessorFailoverManager processorFailoverManager;
    private final TransactionService transactionService
            ;

    @Override
    public String getRoute(Context context) {
        Terminal terminal = context.get(TERMINAL);
        PreTransactionInfo preTransactionInfo = getPreTransactionInfo(context);

        boolean isInternational = preTransactionInfo.isInternational();
        Merchant merchant = terminal.getMerchant();
        String ownerId = merchant.isRoutingEnabled() ? merchant.getMerchantId() : merchant.getInstitution().getInstitutionId();

        List<? extends BaseRule> rules = fetchRules(isInternational, ownerId);

        log.info("Rules count is {}", rules.size());

        String destination = determineDestination(rules, preTransactionInfo.getMerchantType(), context).orElseGet(() -> {
            log.info("No matching rule found. Using default destination: {}", defaultDestination);
            return defaultDestination;
        });

        Boolean applyFailover = context.get(APPLY_FAILOVER, Boolean.FALSE);
        if (applyFailover) {
            Institution institution = context.get(INSTITUTION);
            destination = processorFailoverManager.getCurrentProcessor(institution.getInstitutionId(), destination, preTransactionInfo.isInternational());
        }

        transactionService.calculateFee(context,preTransactionInfo,destination);

        return destination;
    }

    private PreTransactionInfo  getPreTransactionInfo(Context context) {

        ISOMsg msg = (ISOMsg) context.get(REQUEST);

        String terminalCountryCode;
        String currencyCode;
        String merchantType;
        BigDecimal charges = BigDecimal.ZERO;
        BigDecimal amount = convertIsoAmountToLegacy(msg.getString(4));

        if (msg.hasField(126)) {
            Map<String, String> map = Utils.parseField126(msg.getString(126));

            terminalCountryCode = map.getOrDefault("01","0566");
            currencyCode = map.getOrDefault("02","0566");
            merchantType = map.getOrDefault("03","MA");
             charges = new BigDecimal(map.getOrDefault("04", "0")).setScale(2, RoundingMode.HALF_UP);
            log.info("charge is {}",charges);

            context.put(CHARGE,charges);


        } else {
            merchantType = msg.getString("62").substring(0, 2);
            currencyCode = msg.getString(63).substring(4, 8);
            terminalCountryCode = msg.getString(63).substring(0, 4);
        }

        if (terminalCountryCode == null || currencyCode == null || merchantType == null) {
            throw new CoreProcessorException("invalid data passed on");
        }


        //todo. this check should evaluate if the currency is different from the country code. then check if the route is different
        boolean domain = !terminalCountryCode.equalsIgnoreCase(currencyCode);

        //todo handle when it is local but the card is foreign
        msg.unset(126);
        if (internationalCardValidationService.validateInternationalCard(msg.getString(2).substring(0, 6), currencyCode)){
           domain = true;
        }

        log.info("the international domain check is {}", domain);

        return PreTransactionInfo
                .builder()
                .charges(charges)
                .amount(amount)
                .transactionType(TransactionType.fromString(context.get(TXNNAME)))
                .currency(msg.getString(49))
                .isInternational(domain)
                .merchantType(merchantType).build();
    }


    private Optional<String>   determineDestination(List<? extends BaseRule> rules, String merchantType, Context context) {
        return rules.stream()
                .filter(rule -> rule.getMerchantType().equalsIgnoreCase(merchantType))
                .map(rule -> {
                    RuleNameInterface routingRuleService = applicationContext.getBean(rule.getRuleName(), RuleNameInterface.class);
                    boolean passed = routingRuleService.check(rule.getCondition(), context);
                    if (passed) {
                        log.info("Rule {} matched. Setting destination to {}", rule.getRuleName(), rule.getProcessor());
                        return rule.getProcessor();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst();
    }

    private List<? extends BaseRule> fetchRules(boolean isInternational, String ownerId) {
        List<? extends BaseRule> rules = isInternational
                ? internationalRuleRepository.findByOwnerId(ownerId)
                : institutionRoutingRuleRepository.findByOwnerId(ownerId);

        if (rules.isEmpty()) {
            rules = isInternational
                    ? defaultInternationalRuleRepository.findAll()
                    : defaultRoutingRuleRepository.findAll();
        }

        rules.sort(Comparator.comparing(BaseRule::getPrecedence));
        return rules;
    }




}
