package cn.structured.example.audit.client.config;

import cn.structured.audit.api.service.IRemoteAuditService;
import cn.structured.example.audit.client.service.AuditRemoteServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AutoAuditConfiguration {

    @Bean
    public IRemoteAuditService iRemoteAuditService() {
        return new AuditRemoteServiceImpl();
    }
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        log.info("配置Jackson2JsonMessageConverter完成");
        return converter;
    }

}
