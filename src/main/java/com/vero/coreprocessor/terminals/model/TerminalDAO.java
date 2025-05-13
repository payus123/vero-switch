package com.vero.coreprocessor.terminals.model;

import com.vero.coreprocessor.terminals.repository.*;
import lombok.*;
import org.springframework.stereotype.*;

import java.util.*;

@Service @RequiredArgsConstructor
public class TerminalDAO {
    private final TerminalRepository terminalRepository;

    public Optional<Terminal> getTerminal(String terminalID){
        return terminalRepository.findByTerminalId(terminalID);
    }
}
