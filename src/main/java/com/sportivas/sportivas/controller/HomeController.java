package com.sportivas.sportivas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import io.swagger.v3.oas.annotations.Hidden;

@Controller
public class HomeController {

    @GetMapping("/")
    public RedirectView home() {
        return new RedirectView("/app");
    }
    
    @GetMapping("/swagger")
    public RedirectView swagger() {
        return new RedirectView("/swagger-ui.html");
    }
    
    @GetMapping("/api/health")
    @ResponseBody
    @Hidden
    public String healthCheck() {
        return "API is running!";
    }
}