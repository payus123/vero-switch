package com.vero.coreprocessor.ruleengine.controller;

import com.vero.coreprocessor.ruleengine.service.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/routes")
@RequiredArgsConstructor
public class RuleController {
    private final RuleEngine ruleEngine;

    @GetMapping("getRule")
    public ResponseEntity<String>getRoute(){
        try {
            return new ResponseEntity<>(null,HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
