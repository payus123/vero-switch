package com.vero.coreprocessor.keymodule.repository;

import com.vero.coreprocessor.keymodule.model.*;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface TerminalKeysRepository extends JpaRepository <TerminalKeys,Long>{
    Optional<TerminalKeys> findByTerminalId(String terminalID);
}
