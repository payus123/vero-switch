package com.vero.coreprocessor.merchants.repository;

import com.vero.coreprocessor.merchants.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant,Long> {
}
