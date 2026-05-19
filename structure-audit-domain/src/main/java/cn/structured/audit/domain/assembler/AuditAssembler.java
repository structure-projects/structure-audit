package cn.structured.audit.domain.assembler;

import cn.structured.audit.api.dto.AuditDTO;
import cn.structured.audit.api.vo.AuditVO;
import cn.structured.audit.domain.entity.Audit;

/**
 * 审计对象转换器
 * 用于在 AuditDTO、Audit、AuditVO 之间进行转换
 *
 * @author structured
 * @since 2026-01-01
 */
public class AuditAssembler {

    private AuditAssembler() {
    }

    /**
     * 将 Audit 实体转换为 AuditVO
     *
     * @param audit 审计实体
     * @return 审计VO
     */
    public static AuditVO assembler(Audit audit) {
        return AuditVO.builder()
                .id(audit.getId())
                .operationUser(audit.getOperationAlias())
                .operationUserId(audit.getOperationId())
                .action(audit.getAction())
                .module(audit.getModule())
                .operationParams(audit.getOperationParams())
                .operationResult(audit.getOperationResult())
                .status(audit.getStatus())
                .errorMsg(audit.getErrorMsg())
                .ipAddress(audit.getIpAddress())
                .userAgent(audit.getUserAgent())
                .costTime(audit.getCostTime())
                .operationTime(audit.getCreateTime())
                .build();
    }

    /**
     * 将 AuditDTO 转换为 Audit 实体
     *
     * @param audit 审计DTO
     * @return 审计实体
     */
    public static Audit assembler(AuditDTO audit) {
        return Audit.builder()
                .operationAlias(audit.getOperatorName())
                .operationId(audit.getOperatorId())
                .requestId(audit.getRequestId())
                .action(audit.getAction())
                .module(audit.getModule())
                .operationParams(audit.getOperationParams())
                .operationResult(audit.getOperationResult())
                .status(audit.getStatus())
                .errorMsg(audit.getErrorMsg())
                .ipAddress(audit.getIpAddress())
                .userAgent(audit.getUserAgent())
                .costTime(audit.getCostTime())
                .tenantId(audit.getOrganizationId())
                .build();
    }

}
