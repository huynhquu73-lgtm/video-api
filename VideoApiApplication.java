package com.video.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;

@SpringBootApplication
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class VideoApiApplication {

    // Tạo một cái "túi" để chứa tài khoản tạm thời (vì chưa có Database)
    private static Map<String, String> users = new HashMap<>();

    public static void main(String[] args) {
        // Cho sẵn tài khoản admin vào túi
        users.put("admin", "123");
        SpringApplication.run(VideoApiApplication.class, args);
    }

    // 1. HÀM ĐĂNG KÝ (Mày đang thiếu cái này nè!)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        String user = payload.get("username");
        String pass = payload.get("password");

        if (user == null || pass == null || user.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu thông tin!"));
        }

        if (users.containsKey(user)) {
            return ResponseEntity.status(400).body(Map.of("error", "Tài khoản đã tồn tại!"));
        }

        users.put(user, pass); // Lưu vào túi
        return ResponseEntity.ok(Map.of("status", "success", "message", "Đăng ký thành công!"));
    }

    // 2. HÀM ĐĂNG NHẬP (Giờ nó sẽ kiểm tra trong túi users)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String user = payload.get("username");
        String pass = payload.get("password");

        if (users.containsKey(user) && users.get(user).equals(pass)) {
            return ResponseEntity.ok(Map.of("status", "success", "rank", "VIP", "username", user));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Sai tài khoản hoặc mật khẩu!"));
    }

    // 3. HÀM KIỂM TRA TRẠNG THÁI
    @GetMapping("/status")
    public String status() {
        return "Server OK!";
    }
}
