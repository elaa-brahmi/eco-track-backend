package com.example.demo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRawController {

    @PostMapping(value = "/raw-test", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> rawTest(@RequestBody JsonNode payload) {
        return ResponseEntity.ok("IT WORKS — received: " + payload.toPrettyString());
    }
}