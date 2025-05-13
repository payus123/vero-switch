package com.vero.coreprocessor.terminals.repository;

import com.vero.coreprocessor.terminals.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal,Long> {
    Optional<Terminal> findByTerminalId(String terminalID);
}
