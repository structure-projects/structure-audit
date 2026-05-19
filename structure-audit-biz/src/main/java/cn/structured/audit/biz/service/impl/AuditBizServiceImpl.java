package cn.structured.audit.biz.service.impl;

import cn.structured.audit.api.dto.AuditDTO;
import cn.structured.audit.biz.config.AuditStorageConfig;
import cn.structured.audit.biz.service.IAuditBizService;
import cn.structured.audit.biz.service.IAuditEsService;
import cn.structured.audit.domain.assembler.AuditAssembler;
import cn.structured.audit.domain.entity.Audit;
import cn.structured.audit.domain.service.IAuditService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 审计业务服务实现类
 * 负责处理审计请求和响应消息，实现请求-响应数据合并，
 * 并根据配置将审计数据存储到MySQL、ES或两者
 *
 * @author structured
 * @since 2026-01-01
 */
@Slf4j
@Service
@AllArgsConstructor
public class AuditBizServiceImpl implements IAuditBizService {

    private final StringRedisTemplate redisTemplate;
    private final IAuditService auditService;
    private final IAuditEsService auditEsService;
    private final AuditStorageConfig auditStorageConfig;
    private final ObjectMapper objectMapper;

    /**
     * Redis中缓存审计请求数据的key前缀
     */
    private static final String AUDIT_REDIS_PREFIX = "audit:request:";

    /**
     * 处理审计请求消息
     * 将请求数据缓存到Redis中，等待响应消息到达时合并
     *
     * @param auditDTO 审计请求DTO
     */
    @Override
    public void handleRequest(AuditDTO auditDTO) {
        try {
            String key = AUDIT_REDIS_PREFIX + auditDTO.getRequestId();
            String value = objectMapper.writeValueAsString(auditDTO);
            redisTemplate.opsForValue().set(key, value);
            log.info("Audit request cached to Redis, requestId: {}, organizationId: {}",
                    auditDTO.getRequestId(), auditDTO.getOrganizationId());
        } catch (JsonProcessingException e) {
            log.error("Failed to cache audit request to Redis, requestId: {}", auditDTO.getRequestId(), e);
        }
    }

    /**
     * 处理审计响应消息
     * 从Redis中获取请求数据并与响应数据合并，然后保存到数据库
     *
     * @param auditDTO 审计响应DTO
     */
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
                log.warn("Failed to deserialize cached audit request, using response data only, requestId: {}",
                        auditDTO.getRequestId(), e);
            } finally {
                redisTemplate.delete(key);
                log.debug("Cached audit request deleted from Redis, requestId: {}", auditDTO.getRequestId());
            }
        } else {
            log.info("No cached audit request found in Redis, using response data only, requestId: {}",
                    auditDTO.getRequestId());
        }

        saveAudit(mergedAudit);
    }

    /**
     * 合并审计请求DTO和响应DTO
     * 请求数据优先级高于响应数据，特别是：
     * - 操作人信息（operatorId、operatorName）来自请求
     * - 操作行为（action）、模块（module）来自请求
     * - 客户端信息（ipAddress、userAgent）来自请求
     * - 执行结果（result、status、errorMsg、costTime）来自响应
     *
     * @param request 请求DTO
     * @param response 响应DTO
     * @return 合并后的DTO
     */
    private AuditDTO mergeAuditDTO(AuditDTO request, AuditDTO response) {
        AuditDTO merged = new AuditDTO();
        merged.setOrganizationId(request.getOrganizationId() != null ? request.getOrganizationId() : response.getOrganizationId());
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

    /**
     * 保存审计数据
     * 根据配置的存储策略决定数据保存位置
     *
     * @param auditDTO 审计DTO
     */
    public void saveAudit(AuditDTO auditDTO) {
        try {
            Audit audit = AuditAssembler.assembler(auditDTO);
            AuditStorageConfig.StorageType storageType = auditStorageConfig.getType();
            
            log.info("Starting to save audit data, requestId: {}, storageType: {}", 
                    auditDTO.getRequestId(), storageType);
            
            switch (storageType) {
                case MYSQL:
                    saveToMysql(audit, auditDTO);
                    break;
                case ELASTICSEARCH:
                    saveToElasticsearch(audit, auditDTO);
                    break;
                case BOTH:
                    saveToMysql(audit, auditDTO);
                    saveToElasticsearch(audit, auditDTO);
                    break;
            }
            
            log.info("Audit data saved successfully, requestId: {}, storageType: {}", 
                    auditDTO.getRequestId(), storageType);
        } catch (Exception e) {
            log.error("Failed to save audit, requestId: {}", auditDTO.getRequestId(), e);
            throw e;
        }
    }

    /**
     * 保存审计数据到MySQL
     *
     * @param audit 审计实体
     * @param auditDTO 审计DTO（用于日志记录）
     */
    private void saveToMysql(Audit audit, AuditDTO auditDTO) {
        try {
            auditService.save(audit);
            log.info("Audit saved to MySQL successfully, id: {}, requestId: {}, organizationId: {}",
                    audit.getId(), auditDTO.getRequestId(), auditDTO.getOrganizationId());
        } catch (Exception e) {
            log.error("Failed to save audit to MySQL, requestId: {}", auditDTO.getRequestId(), e);
            throw e;
        }
    }

    /**
     * 保存审计数据到Elasticsearch
     *
     * @param audit 审计实体
     * @param auditDTO 审计DTO（用于日志记录）
     */
    private void saveToElasticsearch(Audit audit, AuditDTO auditDTO) {
        try {
            auditEsService.save(audit);
            log.info("Audit saved to Elasticsearch successfully, id: {}, requestId: {}, organizationId: {}",
                    audit.getId(), auditDTO.getRequestId(), auditDTO.getOrganizationId());
        } catch (Exception e) {
            log.error("Failed to save audit to Elasticsearch, requestId: {}", auditDTO.getRequestId(), e);
            throw e;
        }
    }
}
