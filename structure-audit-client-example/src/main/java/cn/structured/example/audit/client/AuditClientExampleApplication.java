package cn.structured.example.audit.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 审计客户端示例启动类
 */
@SpringBootApplication
@EnableFeignClients
// 扫描审计包如果需要使用 domain 功能是需哟啊添加
@ComponentScan(basePackages = {"cn.structured.example.audit.client", "cn.structured.audit.api", "cn.structured.audit.domain.**"})
public class AuditClientExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditClientExampleApplication.class, args);
    }
}
