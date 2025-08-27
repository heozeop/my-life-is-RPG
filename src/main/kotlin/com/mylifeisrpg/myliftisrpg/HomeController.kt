package com.mylifeisrpg.myliftisrpg

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController {
    
    @GetMapping("/")
    fun home(): Map<String, String> {
        return mapOf(
            "message" to "Welcome to MyLiftIsRPG!",
            "status" to "Application is running"
        )
    }
    
    @GetMapping("/api/health")
    fun health(): Map<String, String> {
        return mapOf("status" to "UP")
    }
}