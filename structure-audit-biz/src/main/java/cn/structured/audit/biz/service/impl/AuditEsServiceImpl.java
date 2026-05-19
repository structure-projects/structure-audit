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

@Slf4j
@Service
@AllArgsConstructor
public class AuditEsServiceImpl implements IAuditEsService {

    private final AuditEsRepository auditEsRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void save(Audit audit) {
        try {
            AuditEs auditEs = convertToEs(audit);
            auditEsRepository.save(auditEs);
            log.info("Audit saved to ES, id: {}", audit.getId());
        } catch (Exception e) {
            log.error("Failed to save audit to ES, id: {}", audit.getId(), e);
        }
    }

    @Override
    public void saveAll(List<Audit> audits) {
        try {
            List<AuditEs> auditEsList = audits.stream()
                    .map(this::convertToEs)
                    .collect(Collectors.toList());
            auditEsRepository.saveAll(auditEsList);
            log.info("Bulk saved {} audits to ES", audits.size());
        } catch (Exception e) {
            log.error("Failed to bulk save audits to ES", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            auditEsRepository.deleteById(id);
            log.info("Audit deleted from ES, id: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete audit from ES, id: {}", id, e);
        }
    }

    @Override
    public Audit findById(Long id) {
        try {
            Optional<AuditEs> auditEs = auditEsRepository.findById(id);
            return auditEs.map(this::convertFromEs).orElse(null);
        } catch (Exception e) {
            log.error("Failed to find audit in ES, id: {}", id, e);
            return null;
        }
    }

    @Override
    public List<Audit> searchByCondition(String action, String module, Integer status, String startTime, String endTime) {
        List<Audit> result = new ArrayList<>();
        try {
            Criteria criteria = new Criteria();
            boolean hasCondition = false;

            if (action != null && !action.isEmpty()) {
                criteria = criteria.and("action").is(action);
                hasCondition = true;
            }
            if (module != null && !module.isEmpty()) {
                criteria = criteria.and("module").is(module);
                hasCondition = true;
            }
            if (status != null) {
                criteria = criteria.and("status").is(status);
                hasCondition = true;
            }
            if (startTime != null && !startTime.isEmpty()) {
                criteria = criteria.and("createTime").greaterThanEqual(startTime);
                hasCondition = true;
            }
            if (endTime != null && !endTime.isEmpty()) {
                criteria = criteria.and("createTime").lessThanEqual(endTime);
                hasCondition = true;
            }

            Query query;
            if (hasCondition) {
                query = new CriteriaQuery(criteria).addSort(Sort.by(Sort.Direction.DESC, "createTime"));
            } else {
                query = new CriteriaQuery(new Criteria()).addSort(Sort.by(Sort.Direction.DESC, "createTime"));
            }
            
            SearchHits<AuditEs> searchHits = elasticsearchOperations.search(query, AuditEs.class);

            searchHits.forEach(hit -> result.add(convertFromEs(hit.getContent())));
        } catch (Exception e) {
            log.error("Failed to search audits in ES", e);
        }
        return result;
    }

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
