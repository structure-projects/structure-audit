package cn.structured.audit.biz.configureation;

import cn.structured.audit.api.constant.AuditRabbitConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditRabbitConfig {

    @Bean
    public DirectExchange auditExchange() {
        return new DirectExchange(AuditRabbitConstants.AUDIT_EXCHANGE, true, false);
    }

    @Bean
    public Queue auditRequestQueue() {
        return new Queue(AuditRabbitConstants.AUDIT_REQUEST_QUEUE, true);
    }

    @Bean
    public Queue auditResponseQueue() {
        return new Queue(AuditRabbitConstants.AUDIT_RESPONSE_QUEUE, true);
    }

    @Bean
    public Binding auditRequestBinding(DirectExchange auditExchange, Queue auditRequestQueue) {
        return BindingBuilder.bind(auditRequestQueue).to(auditExchange).with(AuditRabbitConstants.AUDIT_REQUEST_ROUTING_KEY);
    }

    @Bean
    public Binding auditResponseBinding(DirectExchange auditExchange, Queue auditResponseQueue) {
        return BindingBuilder.bind(auditResponseQueue).to(auditExchange).with(AuditRabbitConstants.AUDIT_RESPONSE_ROUTING_KEY);
    }
}
