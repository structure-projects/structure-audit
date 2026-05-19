package cn.structured.audit.biz.task;

import cn.structured.audit.biz.service.IAuditEsService;
import cn.structured.audit.domain.entity.Audit;
import cn.structured.audit.domain.service.IAuditService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审计数据定时任务
 * 负责将历史审计数据从MySQL迁移到ES并清理MySQL中的数据
 *
 * @author structured
 * @since 2026-01-01
 */
@Slf4j
@Component
@AllArgsConstructor
public class AuditTask {

    private final IAuditService auditService;
    private final IAuditEsService auditEsService;

    /**
     * 数据保留天数
     * 超过30天的数据将被迁移到ES并从MySQL中删除
     */
    private static final int RETENTION_DAYS = 30;

    /**
     * 清理和备份历史审计数据
     * 任务名称: cleanAndBackupOldData
     * 执行逻辑:
     * 1. 查询30天前的审计记录
     * 2. 批量写入到Elasticsearch
     * 3. 从MySQL中删除已备份的记录
     */
    @XxlJob("cleanAndBackupOldData")
    public void cleanAndBackupOldData() {
        log.info("===== Starting audit data cleanup and backup task =====");
        
        try {
            LocalDateTime thresholdTime = LocalDateTime.now().minusDays(RETENTION_DAYS);
            log.info("Querying audit records older than {} days, threshold time: {}", RETENTION_DAYS, thresholdTime);
            
            LambdaQueryWrapper<Audit> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.le(Audit::getCreateTime, thresholdTime)
                    .eq(Audit::getDeleted, false);
            
            List<Audit> oldAudits = auditService.list(queryWrapper);
            
            if (oldAudits.isEmpty()) {
                log.info("No old audit records found to backup and clean");
                log.info("===== Audit data cleanup and backup task completed =====");
                return;
            }
            
            log.info("Found {} old audit records to backup", oldAudits.size());
            
            // 备份到ES
            log.info("Starting to backup {} records to ES...", oldAudits.size());
            auditEsService.saveAll(oldAudits);
            log.info("Successfully backed up {} records to ES", oldAudits.size());
            
            // 从MySQL中删除
            List<Long> ids = oldAudits.stream()
                    .map(Audit::getId)
                    .collect(Collectors.toList());
            log.info("Starting to delete {} records from MySQL...", ids.size());
            
            boolean deleted = auditService.removeByIds(ids);
            
            if (deleted) {
                log.info("Successfully deleted {} old audit records from MySQL", ids.size());
            } else {
                log.warn("Failed to delete old audit records from MySQL");
            }
            
            log.info("===== Audit data cleanup and backup task completed successfully =====");
        } catch (Exception e) {
            log.error("===== Audit data cleanup and backup task failed =====", e);
            throw new RuntimeException("Audit data backup and cleanup failed", e);
        }
    }
}
