package cn.structured.audit.biz.service.impl;

import cn.structured.audit.biz.entity.AuditEs;
import cn.structured.audit.biz.repository.AuditEsRepository;
import cn.structured.audit.biz.service.IAuditEsService;
import cn.structured.audit.domain.entity.Audit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 审计ES服务实现类
 * 负责将审计数据存储到Elasticsearch以及从ES查询审计数据
 *
 * @author structured
 * @since 2026-01-01
 */
@Slf4j
@Service
@AllArgsConstructor
public class AuditEsServiceImpl implements IAuditEsService {

    private final AuditEsRepository auditEsRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 保存单个审计记录到ES
     *
     * @param audit 审计实体
     */
    @Override
    public void save(Audit audit) {
        try {
            AuditEs auditEs = convertToEs(audit);
            auditEsRepository.save(auditEs);
            log.info("Audit saved to ES successfully, id: {}, requestId: {}", audit.getId(), audit.getRequestId());
        } catch (Exception e) {
            log.error("Failed to save audit to ES, id: {}, requestId: {}", audit.getId(), audit.getRequestId(), e);
        }
    }

    /**
     * 批量保存审计记录到ES
     *
     * @param audits 审计实体列表
     */
    @Override
    public void saveAll(List<Audit> audits) {
        try {
            List<AuditEs> auditEsList = audits.stream()
                    .map(this::convertToEs)
                    .collect(Collectors.toList());
            auditEsRepository.saveAll(auditEsList);
            log.info("Batch saved {} audit records to ES successfully", audits.size());
        } catch (Exception e) {
            log.error("Failed to batch save audits to ES, count: {}", audits.size(), e);
        }
    }

    /**
     * 根据ID删除ES中的审计记录
     *
     * @param id 审计ID
     */
    @Override
    public void deleteById(Long id) {
        try {
            auditEsRepository.deleteById(id);
            log.info("Audit deleted from ES successfully, id: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete audit from ES, id: {}", id, e);
        }
    }

    /**
     * 根据ID从ES查询审计记录
     *
     * @param id 审计ID
     * @return 审计实体
     */
    @Override
    public Audit findById(Long id) {
        try {
            Optional<AuditEs> auditEs = auditEsRepository.findById(id);
            if (auditEs.isPresent()) {
                log.debug("Found audit in ES, id: {}", id);
                return convertFromEs(auditEs.get());
            }
            log.debug("Audit not found in ES, id: {}", id);
            return null;
        } catch (Exception e) {
            log.error("Failed to find audit in ES, id: {}", id, e);
            return null;
        }
    }

    /**
     * 根据条件从ES查询审计记录
     * 支持按action、module、status、时间范围查询
     *
     * @param action 操作行为
     * @param module 操作模块
     * @param status 操作状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 审计记录列表
     */
    @Override
    public List<Audit> searchByCondition(String action, String module, Integer status, String startTime, String endTime) {
        List<Audit> result = new ArrayList<>();
        try {
            Criteria criteria = new Criteria();
            boolean hasCondition = false;

            if (action != null && !action.isEmpty()) {
                criteria = criteria.and("action").is(action);
                hasCondition = true;
                log.debug("Add search condition: action={}", action);
            }
            if (module != null && !module.isEmpty()) {
                criteria = criteria.and("module").is(module);
                hasCondition = true;
                log.debug("Add search condition: module={}", module);
            }
            if (status != null) {
                criteria = criteria.and("status").is(status);
                hasCondition = true;
                log.debug("Add search condition: status={}", status);
            }
            if (startTime != null && !startTime.isEmpty()) {
                criteria = criteria.and("createTime").greaterThanEqual(startTime);
                hasCondition = true;
                log.debug("Add search condition: startTime={}", startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                criteria = criteria.and("createTime").lessThanEqual(endTime);
                hasCondition = true;
                log.debug("Add search condition: endTime={}", endTime);
            }

            Query query;
            if (hasCondition) {
                query = new CriteriaQuery(criteria).addSort(Sort.by(Sort.Direction.DESC, "createTime"));
            } else {
                query = new CriteriaQuery(new Criteria()).addSort(Sort.by(Sort.Direction.DESC, "createTime"));
            }
            
            log.debug("Executing ES search query...");
            SearchHits<AuditEs> searchHits = elasticsearchOperations.search(query, AuditEs.class);
            
            searchHits.forEach(hit -> result.add(convertFromEs(hit.getContent())));
            log.info("ES search completed, found {} records", result.size());
        } catch (Exception e) {
            log.error("Failed to search audits in ES", e);
        }
        return result;
    }

    /**
     * 将Audit实体转换为AuditEs实体
     *
     * @param audit 审计实体
     * @return ES审计实体
     */
    private AuditEs convertToEs(Audit audit) {
        return AuditEs.builder()
                .id(audit.getId())
                .requestId(audit.getRequestId())
                .operationId(audit.getOperationId())
                .operationAlias(audit.getOperationAlias())
                .action(audit.getAction())
                .module(audit.getModule())
                .operationParams(audit.getOperationParams())
                .operationResult(audit.getOperationResult())
                .status(audit.getStatus())
                .errorMsg(audit.getErrorMsg())
                .ipAddress(audit.getIpAddress())
                .userAgent(audit.getUserAgent())
                .costTime(audit.getCostTime())
                .tenantId(audit.getTenantId())
                .createTime(audit.getCreateTime())
                .updateTime(audit.getUpdateTime())
                .createBy(audit.getCreateBy())
                .updateBy(audit.getUpdateBy())
                .deleted(audit.getDeleted())
                .build();
    }

    /**
     * 将AuditEs实体转换为Audit实体
     *
     * @param auditEs ES审计实体
     * @return 审计实体
     */
    private Audit convertFromEs(AuditEs auditEs) {
        return Audit.builder()
                .id(auditEs.getId())
                .requestId(auditEs.getRequestId())
                .operationId(auditEs.getOperationId())
                .operationAlias(auditEs.getOperationAlias())
                .action(auditEs.getAction())
                .module(auditEs.getModule())
                .operationParams(auditEs.getOperationParams())
                .operationResult(auditEs.getOperationResult())
                .status(auditEs.getStatus())
                .errorMsg(auditEs.getErrorMsg())
                .ipAddress(auditEs.getIpAddress())
                .userAgent(auditEs.getUserAgent())
                .costTime(auditEs.getCostTime())
                .tenantId(auditEs.getTenantId())
                .createTime(auditEs.getCreateTime())
                .updateTime(auditEs.getUpdateTime())
                .createBy(auditEs.getCreateBy())
                .updateBy(auditEs.getUpdateBy())
                .deleted(auditEs.getDeleted())
                .build();
    }
}
