package com.sportivas.sportivas.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class ResourceController {

    @GetMapping(value = "/css/{filename:.+}", produces = "text/css")
    @ResponseBody
    public String getCss(@PathVariable String filename) throws IOException {
        Resource resource = new ClassPathResource("static/css/" + filename);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @GetMapping(value = "/js/{filename:.+}", produces = "application/javascript")
    @ResponseBody
    public String getJs(@PathVariable String filename) throws IOException {
        Resource resource = new ClassPathResource("static/js/" + filename);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}