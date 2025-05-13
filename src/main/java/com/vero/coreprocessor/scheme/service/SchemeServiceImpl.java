package com.vero.coreprocessor.scheme.service;

import com.vero.coreprocessor.scheme.model.*;
import com.vero.coreprocessor.scheme.repository.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;

import java.util.*;
@Service
@Slf4j
@RequiredArgsConstructor
public class SchemeServiceImpl implements SchemeService {
    private final SchemeRepository schemeRepository;
    @Override
    public List<Scheme> getAllSchemes() {
        return schemeRepository.findAll();
    }

    @Override
    public Scheme addScheme(Scheme scheme) {
        if (schemeRepository.findBySchemeName(scheme.getSchemeName()).isPresent()){
            throw new EntityExistsException("Scheme with scheme name already exist"+scheme.getSchemeName());
        }
        return schemeRepository.save(Scheme.builder()
                        .schemeName(scheme.getSchemeName())
                        .regex(scheme.getRegex())
                .build());
    }

    @Override
    public Scheme editScheme(Scheme scheme) {
        Scheme scheme1 = schemeRepository.findById(scheme.getId()).orElse(null);
        if (scheme1==null){
            log.error("scheme not found");
            return null;
        }
        scheme1.setSchemeName(scheme.getSchemeName());
        scheme1.setRegex(scheme.getRegex());
        return schemeRepository.save(scheme1);
    }
}
