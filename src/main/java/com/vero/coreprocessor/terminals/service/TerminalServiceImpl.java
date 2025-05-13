package com.vero.coreprocessor.terminals.service;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.vero.coreprocessor.terminals.model.*;
import com.vero.coreprocessor.terminals.repository.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TerminalServiceImpl implements TerminalService {
    private final TerminalRepository terminalRepository;
    ObjectMapper mapper = new ObjectMapper();
    @Override
    public Terminal findByTerminalID(String terminalID) {
        Terminal terminal = terminalRepository.findByTerminalId(terminalID).orElse(null);
        try {
            log.info("terminal gotten is {}",mapper.writeValueAsString(terminal));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return terminal;
    }
}
