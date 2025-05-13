package com.vero.coreprocessor.ruleengine.repository;

import com.vero.coreprocessor.ruleengine.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

@Repository
public interface DefaultRoutingRuleRepository extends JpaRepository<DefaultLocalRule,Long> {
}
