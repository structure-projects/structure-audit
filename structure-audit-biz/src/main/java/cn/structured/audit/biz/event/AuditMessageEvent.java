package cn.structured.audit.biz.event;

import cn.structured.audit.api.constant.AuditRabbitConstants;
import cn.structured.audit.api.dto.AuditDTO;
import cn.structured.audit.biz.service.IAuditBizService;
import cn.structure.starter.tenant.TenantContextHolder;
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
        try {
            setTenantContext(auditDTO);
            log.info("Received audit request message, requestId: {}, organizationId: {}",
                    auditDTO.getRequestId(), auditDTO.getOrganizationId());
            auditBizService.handleRequest(auditDTO);
        } finally {
            clearTenantContext();
        }
    }

    @RabbitListener(queues = AuditRabbitConstants.AUDIT_RESPONSE_QUEUE)
    public void onResponseMessage(AuditDTO auditDTO) {
        try {
            setTenantContext(auditDTO);
            log.info("Received audit response message, requestId: {}, organizationId: {}",
                    auditDTO.getRequestId(), auditDTO.getOrganizationId());
            auditBizService.handleResponse(auditDTO);
        } finally {
            clearTenantContext();
        }
    }

    private void setTenantContext(AuditDTO auditDTO) {
        if (auditDTO.getOrganizationId() != null) {
            TenantContextHolder.setTenantId(String.valueOf(auditDTO.getOrganizationId()));
            log.debug("Set tenant context, organizationId: {}", auditDTO.getOrganizationId());
        }
    }

    private void clearTenantContext() {
        TenantContextHolder.clear();
        log.debug("Clear tenant context");
    }
}
