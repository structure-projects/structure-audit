package cn.structured.audit.biz.configureation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class RabbitmqConfiguration {

    @PostConstruct
    public void init() {
        log.info("========================================");
        log.info("RabbitmqConfiguration 正在初始化...");
        log.info("========================================");
    }


    @Bean
    public RabbitTemplate createRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

        //推送到server回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) ->
                log.info("ConfirmCallback correlationData:{},ack:{},cause:{}", correlationData, ack, cause));

        //消息返回给生产者, 路由不到队列时返回给发送者  先returnCallback,再 confirmCallback
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) ->
                log.info("ReturnCallback message:{},replyCode:{},replyText:{},exchange:{},routingKey:{}", message, replyCode, replyText, exchange, routingKey));
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        log.info("配置Jackson2JsonMessageConverter完成");
        return converter;
    }

}
