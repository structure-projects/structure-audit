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

@Slf4j
@Component
@AllArgsConstructor
public class AuditTask {

    private final IAuditService auditService;
    private final IAuditEsService auditEsService;

    private static final int RETENTION_DAYS = 30;

    @XxlJob("cleanAndBackupOldData")
    public void cleanAndBackupOldData() {
        log.info("Starting audit data cleanup and backup task");
        
        LocalDateTime thresholdTime = LocalDateTime.now().minusDays(RETENTION_DAYS);
        
        LambdaQueryWrapper<Audit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(Audit::getCreateTime, thresholdTime)
                .eq(Audit::getDeleted, false);
        
        List<Audit> oldAudits = auditService.list(queryWrapper);
        
        if (oldAudits.isEmpty()) {
            log.info("No old audit data to backup and clean");
            return;
        }
        
        log.info("Found {} old audit records to backup", oldAudits.size());
        
        auditEsService.saveAll(oldAudits);
        
        List<Long> ids = oldAudits.stream()
                .map(Audit::getId)
                .collect(Collectors.toList());
        
        boolean deleted = auditService.removeByIds(ids);
        
        if (deleted) {
            log.info("Successfully cleaned {} old audit records from database", ids.size());
        } else {
            log.warn("Failed to delete old audit records from database");
        }
        
        log.info("Audit data cleanup and backup task completed");
    }
}
