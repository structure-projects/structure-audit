package cn.structured.audit.biz.event;

import cn.structured.audit.api.constant.AuditRabbitConstants;
import cn.structured.audit.api.dto.AuditDTO;
import cn.structured.audit.biz.service.IAuditBizService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class AuditMessageEvent {

    private final IAuditBizService auditBizService;

    @RabbitListener(queues = AuditRabbitConstants.AUDIT_REQUEST_QUEUE)
    public void onRequestMessage(AuditDTO auditDTO) {
        log.info("Received audit request message, requestId: {}", auditDTO.getRequestId());
        auditBizService.handleRequest(auditDTO);
    }

    @RabbitListener(queues = AuditRabbitConstants.AUDIT_RESPONSE_QUEUE)
    public void onResponseMessage(AuditDTO auditDTO) {
        log.info("Received audit response message, requestId: {}", auditDTO.getRequestId());
        auditBizService.handleResponse(auditDTO);
    }
}
