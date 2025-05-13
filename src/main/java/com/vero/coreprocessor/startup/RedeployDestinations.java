package com.vero.coreprocessor.startup;

import com.vero.coreprocessor.destinations.repository.*;
import com.vero.coreprocessor.destinations.service.*;
import jakarta.annotation.*;
import lombok.*;
import org.springframework.stereotype.*;

@Component
@RequiredArgsConstructor
public class RedeployDestinations {
    private final DestinationRepository destinationRepository;
    private final ChannelService channelService;

    @PostConstruct
    private void deployAllDestinations(){
       destinationRepository.findAll().forEach(channelService::createChannelMuxConfig);
    }
}
