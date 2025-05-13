package com.vero.coreprocessor.bins.repositories;

import com.vero.coreprocessor.bins.models.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;


@Repository
public interface BinRepository extends JpaRepository<Bin,Long> {

    Optional<Bin> findByBin(String bin);
}
