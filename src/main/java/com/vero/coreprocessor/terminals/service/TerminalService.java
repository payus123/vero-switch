package com.vero.coreprocessor.terminals.service;

import com.vero.coreprocessor.terminals.model.*;

public interface TerminalService {
    Terminal findByTerminalID(String terminalID);
}
