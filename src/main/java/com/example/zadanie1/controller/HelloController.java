package com.example.zadanie1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Привет! Это первый GET-эндпоинт.";
    }

    @GetMapping("/numbers")
    public Map<String, Object> numbers() {
        List<Integer> fibonacci = List.of(0, 1, 1, 2, 3, 5, 8, 13, 21, 34);
        return Map.of(
                "count", fibonacci.size(),
                "values", fibonacci
        );
    }
}
