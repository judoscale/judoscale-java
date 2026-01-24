package com.judoscale.sample;

import com.judoscale.spring.JudoscaleConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    private final JudoscaleConfig judoscaleConfig;

    public HomeController(JudoscaleConfig judoscaleConfig) {
        this.judoscaleConfig = judoscaleConfig;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(name = "sleep", required = false) Double sleepSeconds,
            Model model) throws InterruptedException {
        
        long startTime = System.currentTimeMillis();
        
        if (sleepSeconds != null && sleepSeconds > 0) {
            long sleepMillis = (long) (sleepSeconds * 1000);
            Thread.sleep(sleepMillis);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        model.addAttribute("apiBaseUrl", judoscaleConfig.getApiBaseUrl());
        model.addAttribute("sleepSeconds", sleepSeconds);
        model.addAttribute("requestDuration", duration);
        
        return "home";
    }

    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "OK";
    }
}
