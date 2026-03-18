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
@CrossOrigin(origins = "*") // Cho phép gọi API từ mọi nơi (Frontend, Postman...)
public class VideoApiApplication {

    @Autowired
    private UserRepository userRepository; // Kết nối tới kho dữ liệu Neon

    public static void main(String[] args) {
        SpringApplication.run(VideoApiApplication.class, args);
    }

    // --- 1. CHỨC NĂNG ĐĂNG KÝ ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        String user = payload.get("username");
        String pass = payload.get("password");

        // Kiểm tra đầu vào
        if (user == null || pass == null || user.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nhập thiếu tên hoặc mật khẩu rồi!"));
        }

        // Kiểm tra xem tên này có trong Database chưa
        if (userRepository.existsByUsername(user)) {
            return ResponseEntity.status(400).body(Map.of("error", "Tên tài khoản này đã có người dùng!"));
        }

        // Tạo tài khoản mới:
        // Mặc định Rank = "Member"
        // Mặc định Tokens = 50
        User newUser = new User(user, pass, "Member", 50);
        userRepository.save(newUser); // Lưu vĩnh viễn vào Neon
        
        return ResponseEntity.ok(Map.of("status", "success", "message", "Đăng ký thành công!"));
    }

    // --- 2. CHỨC NĂNG ĐĂNG NHẬP ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String user = payload.get("username");
        String pass = payload.get("password");

        // Tìm người dùng trong Database
        Optional<User> userOpt = userRepository.findByUsername(user);

        // Nếu tìm thấy và mật khẩu khớp
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(pass)) {
            User u = userOpt.get();
            
            // KIỂM TRA RANK ADMIN:
            // Nếu tên đăng nhập là "admin", tao sẽ ép rank nó thành "ADMIN" để mày có quyền tối cao
            String finalRank = u.getRank();
            if (u.getUsername().equalsIgnoreCase("admin")) {
                finalRank = "ADMIN";
            }

            return ResponseEntity.ok(Map.of(
                "status", "success", 
                "rank", finalRank, 
                "username", u.getUsername(),
                "tokens", u.getTokens()
            ));
        }
        
        // Nếu sai mật khẩu hoặc không thấy user
        return ResponseEntity.status(401).body(Map.of("error", "Sai tài khoản hoặc mật khẩu!"));
    }

    // --- 3. KIỂM TRA SERVER ---
    @GetMapping("/status")
    public String status() {
        return "Server & Database Neon đang hoạt động cực tốt!";
    }
}

// --- CẤU TRÚC BẢNG DỮ LIỆU TRONG NEON (USER ENTITY) ---

@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;
    private String rank;  // Member, VIP, hoặc ADMIN
    private int tokens;   // Số tiền/lượt dùng của user

    // Hàm tạo (Constructor)
    public User() {}
    public User(String username, String password, String rank, int tokens) {
        this.username = username;
        this.password = password;
        this.rank = rank;
        this.tokens = tokens;
    }

    // Getters & Setters (Để Java đọc và ghi dữ liệu)
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }
    public int getTokens() { return tokens; }
    public void setTokens(int tokens) { this.tokens = tokens; }
}

// --- GIAO DIỆN THỦ KHO (REPOSITORY) ---
@Repository
interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
