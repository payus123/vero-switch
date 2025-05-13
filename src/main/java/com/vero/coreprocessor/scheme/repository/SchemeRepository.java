package com.vero.coreprocessor.scheme.repository;

import com.vero.coreprocessor.scheme.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface SchemeRepository extends JpaRepository<Scheme,Long> {
    Optional<Scheme>findBySchemeName(String schemeName);
}
