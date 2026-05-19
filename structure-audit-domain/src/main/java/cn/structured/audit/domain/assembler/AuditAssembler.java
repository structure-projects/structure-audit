package cn.structured.audit.domain.assembler;

import cn.structured.audit.api.dto.AuditDTO;
import cn.structured.audit.api.vo.AuditVO;
import cn.structured.audit.domain.entity.Audit;

public class AuditAssembler {

    private AuditAssembler() {
    }

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
                .build();
    }

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
                .build();
    }

}
