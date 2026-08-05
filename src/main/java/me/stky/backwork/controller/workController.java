package me.stky.backwork.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class workController {

    @GetMapping
    public String rdn(){
        return "FOUND ME";
    }
}
