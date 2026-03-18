package com.video.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.*;
import java.util.*;

@SpringBootApplication
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class VideoApiApplication {

    @Autowired
    private UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(VideoApiApplication.class, args);
    }

    // 1. HÀM ĐĂNG KÝ (Lưu thẳng vào Database Neon)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        String user = payload.get("username");
        String pass = payload.get("password");

        if (user == null || pass == null || user.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu thông tin!"));
        }

        if (userRepository.existsByUsername(user)) {
            return ResponseEntity.status(400).body(Map.of("error", "Tài khoản đã tồn tại!"));
        }

        // Tạo user mới, tặng ngay 500 Token làm vốn
        User newUser = new User(user, pass, 500);
        userRepository.save(newUser);
        
        return ResponseEntity.ok(Map.of("status", "success", "message", "Đăng ký thành công!"));
    }

    // 2. HÀM ĐĂNG NHẬP (Kiểm tra dữ liệu từ Database)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String user = payload.get("username");
        String pass = payload.get("password");

        Optional<User> userOpt = userRepository.findByUsername(user);

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(pass)) {
            User u = userOpt.get();
            return ResponseEntity.ok(Map.of(
                "status", "success", 
                "rank", "VIP", 
                "username", u.getUsername(),
                "tokens", u.getTokens()
            ));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Sai tài khoản hoặc mật khẩu!"));
    }

    // 3. HÀM KIỂM TRA TRẠNG THÁI
    @GetMapping("/status")
    public String status() {
        return "Server & Database Neon đang hoạt động!";
    }
}

// --- PHẦN ĐỊNH NGHĨA DỮ LIỆU (DATABASE MODEL) ---

@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;
    private int tokens;

    public User() {}
    public User(String username, String password, int tokens) {
        this.username = username;
        this.password = password;
        this.tokens = tokens;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getTokens() { return tokens; }
    public void setTokens(int tokens) { this.tokens = tokens; }
}

@Repository
interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
