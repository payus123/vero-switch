package com.vero.coreprocessor.notifications.service;

import com.fasterxml.jackson.databind.*;
import com.google.gson.*;
import com.vero.coreprocessor.notifications.dtos.*;
import com.vero.coreprocessor.transactions.model.*;
import com.vero.coreprocessor.transactions.utilities.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private static ConcurrentMap<String, AtomicInteger> failureCounter = new ConcurrentHashMap<>();
    @Value("${failureCounter}")
    private int failureThreshold;
    private final Gson gson;
    private final WebClient webClient;
    @Value("${notification.base-url}")
    private String notificationUrl;
    @Value("${notification.clientId}")
    private String clientId;
    @Value("${notification.clientSecret}")
    private String clientSecret;
    @Value("${notification.recipients}")
    private List<String> to;
    @Value("${notification.cc}")
    private List<String> cc;
    private final ObjectMapper objectMapper;
    private final ProcessorFailoverManager processorFailoverManager;
    private static final Set<String> FAILURE_CODES = Set.of("91", "99","EX");

    @Async
    public void notifyTransactionFailure(Transaction transaction){
        if (transaction == null){
            return;
        }
        String destination = transaction.getDestination();
        failureCounter.putIfAbsent(destination,new AtomicInteger(0));


        if (FAILURE_CODES.contains(transaction.getResponseCode())) {
            processorFailoverManager.recordFailure(transaction.getInstitutionId(), transaction.getDestination(), transaction.isInternational());
            int currentCount = failureCounter.get(destination).incrementAndGet();

            if (currentCount > failureThreshold) {
                EmailRequest request = EmailRequest.builder()
                        .subject("URGENT!! TRANSACTION FAILURE NOTIFICATION")
                        .to(to)
                        .cc(cc)
                        .content(message(destination, transaction, currentCount))
                        .build();
                sendEmailNotification(request);
                failureCounter.get(destination).set(0);
                // TODO: auto switch
            }
        } else {
            failureCounter.get(destination).set(0);
        }


    }

    @SneakyThrows
    private String message(String destination, Transaction transaction, int count){
        String transactionString = objectMapper.writeValueAsString(transaction);
        return destination+" processor transaction is currently failing "+count+" consecutively"+". sample transaction log :: "+transaction.getRrn()+ " "+transactionString;
    }

    private void sendEmailNotification(EmailRequest message){
        webClient.post()
                .uri(URI.create(notificationUrl))
                .bodyValue(message)
                .header("Client-ID",clientId)
                .header("Secret-Key",clientSecret)
                .retrieve()
                .bodyToMono(NotificationResponse.class)
                .flatMap(response -> {
                    log.info("notification successfully sent to email {}",response);
                    return Mono.empty();
                }).onErrorResume(throwable -> {
                    log.error("error sending email notification. {}",throwable.getMessage());
                    return Mono.empty();
                }).subscribe();

    }
}
