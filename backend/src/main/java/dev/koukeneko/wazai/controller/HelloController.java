package dev.koukeneko.wazai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController // This annotation indicates that this class is a RESTful controller
public class HelloController {

    // Define a GET endpoint at the root URL ("/api/hello")
    @GetMapping("/api/hello")
    public Map<String, String> sayHello(){
        // Return a simple JSON response
        Map<String, String> response = new HashMap<>();
        response.put("project", "Wazai");
        response.put("status", "alive");
        response.put("message", "Hello, World from Spring Boot!");

        return response;
    }
}
