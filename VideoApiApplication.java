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
@CrossOrigin(origins = "*") // Cho phép mọi trang web khác gọi đến API này
public class VideoApiApplication {

    @Autowired
    private UserRepository userRepository; // Kết nối với "thủ kho" Database

    public static void main(String[] args) {
        SpringApplication.run(VideoApiApplication.class, args);
    }

    // --- 1. HÀM ĐĂNG KÝ (Tạo tài khoản mới) ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        String user = payload.get("username");
        String pass = payload.get("password");

        // Kiểm tra xem người dùng có nhập thiếu gì không
        if (user == null || pass == null || user.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mày quên nhập tên hoặc mật khẩu rồi!"));
        }

        // Kiểm tra xem tên này đã có ai dùng chưa
        if (userRepository.existsByUsername(user)) {
            return ResponseEntity.status(400).body(Map.of("error", "Tên này có thằng khác dùng rồi mày ơi!"));
        }

        // TẠO NGƯỜI DÙNG MỚI: 
        // Rank mặc định là "Member" (Không còn là VIP chùa nữa)
        // Tặng 50 Tokens làm vốn thôi (Cho 500 nhanh phá sản lắm)
        User newUser = new User(user, pass, "Member", 50);
        userRepository.save(newUser); // Lệnh lưu xuống Database Neon
        
        return ResponseEntity.ok(Map.of("status", "success", "message", "Đăng ký thành công rồi đó!"));
    }

    // --- 2. HÀM ĐĂNG NHẬP (Kiểm tra đúng sai) ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String user = payload.get("username");
        String pass = payload.get("password");

        // Tìm thằng người dùng trong Database theo cái tên (username)
        Optional<User> userOpt = userRepository.findByUsername(user);

        // Nếu tìm thấy người dùng VÀ mật khẩu khớp nhau
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(pass)) {
            User u = userOpt.get(); // Lấy dữ liệu thằng đó ra
            return ResponseEntity.ok(Map.of(
                "status", "success", 
                "rank", u.getRank(),       // Trả về Rank thật (Member hoặc VIP)
                "username", u.getUsername(),
                "tokens", u.getTokens()    // Trả về số Token thật nó đang có
            ));
        }
        // Nếu sai thì đuổi nó ra
        return ResponseEntity.status(401).body(Map.of("error", "Sai tên hoặc mật khẩu, nhìn kỹ lại đi!"));
    }

    // --- 3. HÀM KIỂM TRA SERVER ---
    @GetMapping("/status")
    public String status() {
        return "Server & Database Neon đang chạy ngon lành cành đào!";
    }
}

// --- PHẦN ĐỊNH NGHĨA CẤU TRÚC BẢNG DỮ LIỆU (DATABASE MODEL) ---

@Entity // Đánh dấu đây là một bảng trong Database
@Table(name = "users") // Tên bảng là "users"
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tự động tăng ID (1, 2, 3...)
    private Long id;

    @Column(unique = true) // Tên tài khoản không được trùng nhau
    private String username;
    private String password;
    private String rank; // Cột lưu Rank (Member/VIP)
    private int tokens;  // Cột lưu số tiền (Token)

    // Hàm khởi tạo (Constructor) - Giúp tạo User nhanh hơn
    public User() {}
    public User(String username, String password, String rank, int tokens) {
        this.username = username;
        this.password = password;
        this.rank = rank;
        this.tokens = tokens;
    }

    // Mấy cái Getters này để Java lấy dữ liệu ra dùng
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRank() { return rank; }
    public int getTokens() { return tokens; }
    
    // Mấy cái Setters này để thay đổi dữ liệu (ví dụ khi nạp Token hoặc lên VIP)
    public void setRank(String rank) { this.rank = rank; }
    public void setTokens(int tokens) { this.tokens = tokens; }
}

// --- THỦ KHO (REPOSITORY) ---
// Đây là nơi thực hiện các lệnh: Tìm, Lưu, Xóa, Sửa trong Database
@Repository
interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // Tìm user theo tên
    boolean existsByUsername(String username);      // Kiểm tra xem tên đã tồn tại chưa
}
