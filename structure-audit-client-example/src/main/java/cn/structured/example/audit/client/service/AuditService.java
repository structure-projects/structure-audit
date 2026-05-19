package cn.structured.example.audit.client.service;

import cn.structured.audit.api.constant.AuditRabbitConstants;
import cn.structured.audit.api.dto.AuditDTO;
import cn.structured.audit.api.service.IRemoteAuditService;
import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class AuditService {

    private final RabbitTemplate rabbitTemplate;

    private final IRemoteAuditService remoteAuditService;

    public String sendRequestMessage(String action, String module, String operatorId,
                                     String operatorName, Object params, HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString();

        AuditDTO auditDTO = new AuditDTO();
        auditDTO.setRequestId(requestId);
        auditDTO.setOperatorId(operatorId);
        auditDTO.setOperatorName(operatorName);
        auditDTO.setAction(action);
        auditDTO.setModule(module);
        auditDTO.setOperationParams(params != null ? JSON.toJSONString(params) : null);
        auditDTO.setIpAddress(getClientIp(request));
        auditDTO.setUserAgent(request != null ? request.getHeader("User-Agent") : null);

        try {
            rabbitTemplate.convertAndSend(
                    AuditRabbitConstants.AUDIT_EXCHANGE,
                    AuditRabbitConstants.AUDIT_REQUEST_ROUTING_KEY,
                    auditDTO
            );
            log.info("MQ请求消息发送成功, requestId: {}, action: {}", requestId, action);
        } catch (Exception e) {
            log.error("MQ请求消息发送失败, requestId: {}", requestId, e);
        }

        return requestId;
    }

    public void sendSuccessResponse(String requestId, Object result, long startTime) {
        AuditDTO auditDTO = new AuditDTO();
        auditDTO.setRequestId(requestId);
        auditDTO.setStatus(1);
        auditDTO.setOperationResult(result != null ? JSON.toJSONString(result) : null);
        auditDTO.setCostTime(System.currentTimeMillis() - startTime);

        try {
            rabbitTemplate.convertAndSend(
                    AuditRabbitConstants.AUDIT_EXCHANGE,
                    AuditRabbitConstants.AUDIT_RESPONSE_ROUTING_KEY,
                    auditDTO
            );
            log.info("MQ成功响应消息发送成功, requestId: {}", requestId);
        } catch (Exception e) {
            log.error("MQ成功响应消息发送失败, requestId: {}", requestId, e);
        }
    }

    public void sendFailResponse(String requestId, String errorMsg, long startTime) {
        AuditDTO auditDTO = new AuditDTO();
        auditDTO.setRequestId(requestId);
        auditDTO.setStatus(0);
        auditDTO.setErrorMsg(errorMsg);
        auditDTO.setCostTime(System.currentTimeMillis() - startTime);

        try {
            rabbitTemplate.convertAndSend(
                    AuditRabbitConstants.AUDIT_EXCHANGE,
                    AuditRabbitConstants.AUDIT_RESPONSE_ROUTING_KEY,
                    auditDTO
            );
            log.info("MQ失败响应消息发送成功, requestId: {}", requestId);
        } catch (Exception e) {
            log.error("MQ失败响应消息发送失败, requestId: {}", requestId, e);
        }
    }

    public void addAuditByHttp(String action, String module, String operatorId,
                               String operatorName, Object params, Object result,
                               Integer status, String errorMsg, String ipAddress, Long costTime) {
        AuditDTO auditDTO = new AuditDTO();
        auditDTO.setRequestId(UUID.randomUUID().toString());
        auditDTO.setOperatorId(operatorId);
        auditDTO.setOperatorName(operatorName);
        auditDTO.setAction(action);
        auditDTO.setModule(module);
        auditDTO.setOperationParams(params != null ? JSON.toJSONString(params) : null);
        auditDTO.setOperationResult(result != null ? JSON.toJSONString(result) : null);
        auditDTO.setStatus(status);
        auditDTO.setErrorMsg(errorMsg);
        auditDTO.setIpAddress(ipAddress);
        auditDTO.setCostTime(costTime);

        try {
            remoteAuditService.saveAuditRecord(auditDTO);
            log.info("HTTP审计日志添加成功, action: {}", action);
        } catch (Exception e) {
            log.error("HTTP审计日志添加失败, action: {}", action, e);
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
}
