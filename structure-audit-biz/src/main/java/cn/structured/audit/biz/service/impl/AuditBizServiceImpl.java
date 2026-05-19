package cn.structured.audit.biz.service.impl;

import cn.structured.audit.api.dto.AuditDTO;
import cn.structured.audit.biz.service.IAuditBizService;
import cn.structured.audit.domain.assembler.AuditAssembler;
import cn.structured.audit.domain.entity.Audit;
import cn.structured.audit.domain.service.IAuditService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AuditBizServiceImpl implements IAuditBizService {

    private final StringRedisTemplate redisTemplate;

    private final IAuditService auditService;

    private final ObjectMapper objectMapper;

    private static final String AUDIT_REDIS_PREFIX = "audit:request:";

    @Override
    public void handleRequest(AuditDTO auditDTO) {
        try {
            String key = AUDIT_REDIS_PREFIX + auditDTO.getRequestId();
            String value = objectMapper.writeValueAsString(auditDTO);
            redisTemplate.opsForValue().set(key, value);
            log.info("Audit request cached to Redis, requestId: {}", auditDTO.getRequestId());
        } catch (JsonProcessingException e) {
            log.error("Failed to cache audit request to Redis, requestId: {}", auditDTO.getRequestId(), e);
        }
    }

    @Override
    public void handleResponse(AuditDTO auditDTO) {
        String key = AUDIT_REDIS_PREFIX + auditDTO.getRequestId();
        String cachedRequest = redisTemplate.opsForValue().get(key);
        
        AuditDTO mergedAudit = auditDTO;
        
        if (cachedRequest != null) {
            log.info("Found cached audit request in Redis, merging data, requestId: {}", auditDTO.getRequestId());
            try {
                AuditDTO requestAudit = objectMapper.readValue(cachedRequest, AuditDTO.class);
                mergedAudit = mergeAuditDTO(requestAudit, auditDTO);
            } catch (JsonProcessingException e) {
                log.warn("Failed to deserialize cached audit request, using response data only, requestId: {}", auditDTO.getRequestId(), e);
            } finally {
                redisTemplate.delete(key);
            }
        } else {
            log.info("No cached audit request found in Redis, using response data only, requestId: {}", auditDTO.getRequestId());
        }
        
        saveAudit(mergedAudit);
    }

    private AuditDTO mergeAuditDTO(AuditDTO request, AuditDTO response) {
        AuditDTO merged = new AuditDTO();
        merged.setOperatorId(request.getOperatorId() != null ? request.getOperatorId() : response.getOperatorId());
        merged.setOperatorName(request.getOperatorName() != null ? request.getOperatorName() : response.getOperatorName());
        merged.setRequestId(request.getRequestId() != null ? request.getRequestId() : response.getRequestId());
        merged.setAction(request.getAction() != null ? request.getAction() : response.getAction());
        merged.setModule(request.getModule() != null ? request.getModule() : response.getModule());
        merged.setOperationParams(request.getOperationParams() != null ? request.getOperationParams() : response.getOperationParams());
        merged.setOperationResult(response.getOperationResult());
        merged.setStatus(response.getStatus());
        merged.setErrorMsg(response.getErrorMsg());
        merged.setIpAddress(request.getIpAddress() != null ? request.getIpAddress() : response.getIpAddress());
        merged.setUserAgent(request.getUserAgent() != null ? request.getUserAgent() : response.getUserAgent());
        merged.setCostTime(response.getCostTime());
        return merged;
    }

    public void saveAudit(AuditDTO auditDTO) {
        try {
            Audit audit = AuditAssembler.assembler(auditDTO);
            auditService.save(audit);
            log.info("Audit saved successfully, requestId: {}", auditDTO.getRequestId());
        } catch (Exception e) {
            log.error("Failed to save audit, requestId: {}", auditDTO.getRequestId(), e);
            throw e;
        }
    }
}
