package cn.structured.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 审计启动类
 *
 * @author chuck
 * @since 2021/12/23
 */
@SpringBootApplication
public class AuditApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuditApplication.class, args);
    }
}
