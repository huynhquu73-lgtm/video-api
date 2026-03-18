package com.video.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@SpringBootApplication
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class VideoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoApiApplication.class, args);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String user = payload.get("username");
        String pass = payload.get("password");

        if ("admin".equals(user) && "123".equals(pass)) {
            return ResponseEntity.ok(Map.of("status", "success", "rank", "VIP"));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Sai tài khoản!"));
    }

    @GetMapping("/status")
    public String status() {
        return "Server đang chạy cực mượt!";
    }
}
