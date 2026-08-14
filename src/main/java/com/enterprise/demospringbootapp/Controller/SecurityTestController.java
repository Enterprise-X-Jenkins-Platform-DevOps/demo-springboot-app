package com.enterprise.demospringbootapp.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/security-test")
public class SecurityTestController {

    private static final String DB_PASSWORD = "SuperSecretPassword123";

    @GetMapping
    public String securityTest() {
        return "Security test endpoint";
    }
}