package com.vero.coreprocessor.ruleengine.service;

import com.vero.coreprocessor.bins.models.*;
import com.vero.coreprocessor.bins.repositories.*;
import com.vero.coreprocessor.ruleengine.dtos.*;
import lombok.*;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.*;

import java.net.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class BinService {
    private final WebClient webClient;
    private final BinRepository binRepository;


    @Cacheable(value = "binCache", key = "#bin", unless = "#result == null")
    public Bin fetchBinDetails(String bin) {

        return binRepository.findByBin(bin).orElseGet(()->{

            URI uri = URI.create("https://api.paystack.co/decision/bin/" + bin);

            BinResponseDto response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(BinResponseDto.class)
                    .onErrorResume(throwable -> Mono.just(new BinResponseDto()))
                    .block();

            if (response == null || response.getData() == null || !response.isStatus()) {
                return null;
            }

            Bin binData = fromDto(response.getData());
            CompletableFuture.runAsync(()->binRepository.save(binData));
            return binData;
        });

    }
    public static Bin fromDto(BinResponseDto.Data data) {
        return Bin.builder()
                .bin(data.getBin())
                .brand(data.getBrand())
                .currency(data.getCurrency())
                .bank(data.getBank())
                .countryName(data.getCountryName())
                .build();
    }

}
