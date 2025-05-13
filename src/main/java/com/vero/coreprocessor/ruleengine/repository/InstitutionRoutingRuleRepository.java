package com.vero.coreprocessor.ruleengine.repository;

import com.vero.coreprocessor.ruleengine.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface InstitutionRoutingRuleRepository extends JpaRepository<RoutingRules,Long> {
    List<RoutingRules> findByOwnerId(String id);
}
