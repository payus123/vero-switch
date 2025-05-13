package com.vero.coreprocessor.transactions.services;

import com.vero.coreprocessor.components.Context;

import com.vero.coreprocessor.destinations.enums.*;
import com.vero.coreprocessor.destinations.model.*;
import com.vero.coreprocessor.destinations.repository.*;
import com.vero.coreprocessor.exceptions.*;
import com.vero.coreprocessor.merchants.model.Merchant;
import com.vero.coreprocessor.ruleengine.dtos.*;
import com.vero.coreprocessor.terminals.model.Terminal;
import com.vero.coreprocessor.transactions.dtos.TransactionDto;
import com.vero.coreprocessor.transactions.enums.*;
import com.vero.coreprocessor.transactions.model.Transaction;
import com.vero.coreprocessor.transactions.repository.TransactionRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Service;

import java.math.*;
import java.util.concurrent.CompletableFuture;

import static com.vero.coreprocessor.constants.Constants.*;
import static com.vero.coreprocessor.rabbitmq.RabbitMqConstants.TRANSACTION_EXCHANGE;
import static com.vero.coreprocessor.rabbitmq.RabbitMqConstants.TRANSACTION_ROUTING_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final RabbitTemplate template;
    private final ModelMapper modelMapper;
    private final DestinationRepository destinationRepository;
    private final DestinationPricingRepository destinationPricingRepository;

    @Override
    public Transaction saveTransaction(Context context, ISOMsg req) {
        if (!req.getString(0).startsWith("08")) {
            Terminal terminal = context.get(TERMINAL);
            Merchant merchant = terminal.getMerchant();
            String institutionId = merchant.getInstitution().getInstitutionId();
            Transaction transaction = Transaction.builder()
                    .mti(req.getString(0))
                    .maskedPan(ISOUtil.protect(req.getString(2), '*'))
                    .procCode(req.getString(3))
                    .transactionAmount(req.getString(4))
                    .tranDateAndTime(req.getString(7))
                    .stan(req.getString(11))
                    .tranTime(req.getString(12))
                    .tranDate(req.getString(13))
                    .cardExpDate(req.getString(14))
                    .settlementDate(req.getString(15))
                    .merchantCategoryCode(req.getString(18))
                    .posEntryMode(req.getString(22))
                    .cardSeqNumber(req.getString(23))
                    .posConditionCode(req.getString(25))
                    .transactionFee(req.getString(28))
                    .processingFee(req.getString(30))
                    .acquiringInstitutionID(req.getString(32))
                    .forwardingInstitutionID(req.getString(33))
                    .track2(ISOUtil.protect(req.getString(35), '_'))
                    .rrn(req.getString(37))
                    .authorizationID(req.getString(38))
                    .responseCode(req.getString(39))
                    .serviceRestrictionCode(req.getString(40))
                    .terminalID(req.getString(41))
                    .merchantID(merchant.getMerchantId())
                    .merchantNameAndLocation(req.getString(43))
                    .currencyCode(req.getString(49))
                    .additionalAmounts(req.getString(54))
                    .accountID1(req.getString(102))
                    .accountID2(req.getString(103))
                    .posDataCode(req.getString(123))
                    .merchant(merchant)
                    .charges(context.get(CHARGE))
                    .institutionId(institutionId)
                    .merchantUniqueReference(merchant.getMerchantUniqueReference())
                    .destination(context.get(DESTINATION))
                    .destinationFee(context.get(DESTINATION_FEE))
                    .switchTid(req.getString(59))
                    .build();
            return transactionRepository.save(transaction);
        }
        return null;
    }


    /**
     * we use charges to know if the request is not from omni-pro
     **/
    @Override
    public void postProcess(Transaction transaction) {
        CompletableFuture.runAsync(() -> {
            if (!transaction.getMti().startsWith("08")) {
                log.info("sending transaction {} to merchant service", transaction.getRrn());
                try {
                    TransactionDto transactionDto = modelMapper.map(transaction, TransactionDto.class);
                    template.convertAndSend(TRANSACTION_EXCHANGE, TRANSACTION_ROUTING_KEY, transactionDto);
                } catch (Exception e) {
                    log.error("error occurred sending transaction {} to merchant service: {}", transaction.getRrn(), e.getMessage());
                }
            }
        });

    }

    @SneakyThrows
    public boolean checkDuplicateTransaction(ISOMsg request) {
        ISOMsg msg = (ISOMsg) request.clone();
        String rrn = request.getString(37);
        String stan = request.getString(11);
        msg.setResponseMTI();
        String mti = msg.getMTI();
        return !transactionRepository.findUniqueTransaction(rrn,stan,mti).isPresent();
    }

    @SneakyThrows
    public Transaction findOriginalTransaction(String stan, String mti, String dateTime) {
        return transactionRepository.findOriginalTransaction(stan,mti,dateTime)
                .orElse(null);
    }



    @Async
    public void calculateFee(Context context, PreTransactionInfo preTransactionInfo, String destination) {
        if (preTransactionInfo.getTransactionType() != TransactionType.PURCHASE)
            return;


        DestinationPricing destinationPricing = destinationPricingRepository
                .resolvePricing(preTransactionInfo.getTransactionType(),destination, preTransactionInfo.getCurrency())
                .orElseThrow(CoreProcessorException::new);

        PricingType pricingType = destinationPricing.getPricingType();
        BigDecimal lowerBound = destinationPricing.getLowerBound();
        BigDecimal upperBound = destinationPricing.getUpperBound();
        BigDecimal value = destinationPricing.getValue();
        BigDecimal amount = preTransactionInfo.getAmount();

        BigDecimal destinationFee = switch (pricingType) {
            case PERCENTAGE -> {
                BigDecimal fee = amount.multiply(value)
                        .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
                yield (upperBound != null && fee.compareTo(upperBound) > 0) ? upperBound
                        : (lowerBound != null && fee.compareTo(lowerBound) < 0) ? lowerBound
                        : fee;
            }
            case FLAT -> value;
        };

        context.put(DESTINATION_FEE, destinationFee);
    }

}
