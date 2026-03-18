package com.video.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@SpringBootApplication
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Cho phép Spck Editor gọi tới
public class VideoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoApiApplication.class, args);
    }

    // Dữ liệu User giả lập (Thay cho Database để chạy FREE)
    private static final Map<String, String[]> USERS = Map.of(
        "admin", new String[]{"123", "VIP"},
        "ducnhan", new String[]{"12345", "VÀNG"},
        "khach", new String[]{"111", "ĐỒNG"}
    );

    // API Đăng nhập & Kiểm tra Rank
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        if (USERS.containsKey(username) && USERS.get(username)[0].equals(password)) {
            String rank = USERS.get(username)[1];
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "rank", rank,
                "message", "Chào mừng " + username + " (Hạng: " + rank + ")"
            ));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Sai tài khoản hoặc mật khẩu!"));
    }

    // API Lấy danh sách Video (Có phân quyền theo Rank)
    @GetMapping("/videos")
    public ResponseEntity<?> getVideos(@RequestParam(defaultValue = "ĐỒNG") String userRank) {
        // Logic đơn giản: Nếu là VIP thì thấy mọi video, Đồng thì thấy hạn chế
        return ResponseEntity.ok(Map.of("rank_hien_tai", userRank, "noidung", "Danh sách video đã được lọc..."));
    }
}

// ==========================================
// TƯỜNG LỬA BẢO MẬT (FIREWALL) - CHỐNG SPAM
// ==========================================
@Component
class SecurityFirewall implements Filter {
    
    // Lưu vết IP để chặn nếu gửi request quá nhanh (Rate Limiting)
    private final Map<String, Long> ipLogs = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String ip = req.getRemoteAddr();
        long now = System.currentTimeMillis();

        // Nếu 1 IP gửi request liên tục dưới 0.5 giây -> CHẶN NGAY
        if (ipLogs.containsKey(ip) && (now - ipLogs.get(ip) < 500)) {
            res.setStatus(429); // Too Many Requests
            res.setHeader("Content-Type", "application/json");
            res.getWriter().write("{\"canh_bao\": \"Tường lửa: Bạn đang spam! Hãy thử lại sau.\"}");
            return;
        }

        ipLogs.put(ip, now);
        chain.doFilter(request, response);
    }
          }
