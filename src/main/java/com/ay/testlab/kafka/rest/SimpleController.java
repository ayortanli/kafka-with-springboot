package com.ay.testlab.kafka.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleController {

    @RequestMapping("/Hello")
    public String index() {
        return "<b>Greetings from Spring Boot!<b>";
    }
}
