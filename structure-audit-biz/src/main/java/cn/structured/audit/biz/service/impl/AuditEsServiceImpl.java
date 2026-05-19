package cn.structured.audit.biz.service.impl;

import cn.structured.audit.biz.service.IAuditEsService;
import cn.structured.audit.domain.entity.Audit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class AuditEsServiceImpl implements IAuditEsService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String ES_INDEX_PREFIX = "audit:es:";

    @Override
    public void save(Audit audit) {
        try {
            String key = ES_INDEX_PREFIX + audit.getId();
            String value = objectMapper.writeValueAsString(audit);
            redisTemplate.opsForValue().set(key, value);
            log.info("Audit saved to ES (simulated), id: {}", audit.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to save audit to ES, id: {}", audit.getId(), e);
        }
    }

    @Override
    public void saveAll(List<Audit> audits) {
        audits.forEach(this::save);
        log.info("Bulk saved {} audits to ES (simulated)", audits.size());
    }

    @Override
    public void deleteById(Long id) {
        try {
            String key = ES_INDEX_PREFIX + id;
            redisTemplate.delete(key);
            log.info("Audit deleted from ES (simulated), id: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete audit from ES, id: {}", id, e);
        }
    }

    @Override
    public Audit findById(Long id) {
        try {
            String key = ES_INDEX_PREFIX + id;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, Audit.class);
            }
        } catch (Exception e) {
            log.error("Failed to find audit in ES, id: {}", id, e);
        }
        return null;
    }

    @Override
    public List<Audit> searchByCondition(String action, String module, Integer status, String startTime, String endTime) {
        log.warn("ES search not implemented, returning empty list");
        return new ArrayList<>();
    }
}
