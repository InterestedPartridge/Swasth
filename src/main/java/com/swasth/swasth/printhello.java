package com.swasth.swasth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class printhello {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, World!";
    }

}