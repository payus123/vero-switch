package com.vero.coreprocessor.institution.repository;

import com.vero.coreprocessor.institution.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution,Long> {
    boolean existsByInstitutionId(String institutionId);

}
