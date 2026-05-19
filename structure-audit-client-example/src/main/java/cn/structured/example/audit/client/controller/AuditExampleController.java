package cn.structured.example.audit.client.controller;

import cn.structured.audit.api.aop.AuditLog;
import cn.structured.example.audit.client.service.AuditService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/example")
@AllArgsConstructor
public class AuditExampleController {

    private final AuditService auditService;

    @PostMapping("/mq/login")
    public ResponseEntity<Map<String, Object>> loginByMq(@RequestBody LoginRequest request,
                                                          HttpServletRequest httpRequest) {
        long startTime = System.currentTimeMillis();

        String requestId = auditService.sendRequestMessage(
                "用户登录",
                "用户模块",
                "user-001",
                "张三",
                request,
                httpRequest
        );

        try {
            log.info("执行登录业务逻辑...");
            Thread.sleep(200);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("token", "JWT_TOKEN_123456");

            auditService.sendSuccessResponse(requestId, result, startTime);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            auditService.sendFailResponse(requestId, e.getMessage(), startTime);
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/mq/export")
    public ResponseEntity<Map<String, Object>> exportByMq(@RequestBody ExportRequest request,
                                                          HttpServletRequest httpRequest) {
        long startTime = System.currentTimeMillis();

        String requestId = auditService.sendRequestMessage(
                "数据导出",
                "报表模块",
                "user-002",
                "李四",
                request,
                httpRequest
        );

        try {
            log.info("执行数据导出...");
            Thread.sleep(500);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "导出成功");
            result.put("exportedCount", 1000);

            auditService.sendSuccessResponse(requestId, result, startTime);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            auditService.sendFailResponse(requestId, e.getMessage(), startTime);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    @PostMapping("/http/order")
    public ResponseEntity<Map<String, Object>> createOrderByHttp(@RequestBody OrderRequest request,
                                                                HttpServletRequest httpRequest) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("创建订单...");
            Thread.sleep(300);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "订单创建成功");
            result.put("orderId", "ORD-20260519001");

            auditService.addAuditByHttp(
                    "创建订单",
                    "订单模块",
                    "user-001",
                    "张三",
                    request,
                    result,
                    1,
                    null,
                    getClientIp(httpRequest),
                    System.currentTimeMillis() - startTime
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            auditService.addAuditByHttp(
                    "创建订单",
                    "订单模块",
                    "user-001",
                    "张三",
                    request,
                    null,
                    0,
                    e.getMessage(),
                    getClientIp(httpRequest),
                    System.currentTimeMillis() - startTime
            );
            throw new RuntimeException("创建订单失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/http/user/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUserByHttp(@PathVariable String userId,
                                                               HttpServletRequest httpRequest) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("删除用户: {}", userId);
            Thread.sleep(150);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "用户删除成功");

            auditService.addAuditByHttp(
                    "删除用户",
                    "用户模块",
                    "admin",
                    "管理员",
                    userId,
                    result,
                    1,
                    null,
                    getClientIp(httpRequest),
                    System.currentTimeMillis() - startTime
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            auditService.addAuditByHttp(
                    "删除用户",
                    "用户模块",
                    "admin",
                    "管理员",
                    userId,
                    null,
                    0,
                    e.getMessage(),
                    getClientIp(httpRequest),
                    System.currentTimeMillis() - startTime
            );
            throw new RuntimeException("删除用户失败: " + e.getMessage());
        }
    }

    @AuditLog(value = "用户注册", module = "用户模块")
    @PostMapping("/annotation/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        log.info("执行用户注册...");

        try {
            Thread.sleep(250);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "注册成功");
            result.put("userId", "user-003");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            throw new RuntimeException("注册失败: " + e.getMessage());
        }
    }

    @AuditLog(value = "修改密码", module = "用户模块")
    @PostMapping("/annotation/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody ChangePasswordRequest request) {
        log.info("执行修改密码...");

        try {
            Thread.sleep(100);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "密码修改成功");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            throw new RuntimeException("修改密码失败: " + e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class ExportRequest {
        private String type;
        private String startTime;
        private String endTime;
    }

    @Data
    public static class OrderRequest {
        private String productId;
        private Integer quantity;
        private String address;
    }

    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
    }

    @Data
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;
    }
}
