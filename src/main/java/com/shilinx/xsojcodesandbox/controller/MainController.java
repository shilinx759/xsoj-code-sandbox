package com.shilinx.xsojcodesandbox.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author slx
 */
@RestController("/")
public class MainController {

    @GetMapping("/test")
    public void getTest() {
        System.out.println("ok");
    }
}
