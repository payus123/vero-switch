package com.vero.coreprocessor.scheme.service;

import com.vero.coreprocessor.scheme.model.*;

import java.util.*;

public interface SchemeService {
    List<Scheme> getAllSchemes();

    Scheme addScheme(Scheme scheme);

    Scheme editScheme(Scheme scheme);
}
