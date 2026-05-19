package cn.structured.audit.biz.service;

import cn.structured.audit.api.dto.AuditDTO;

public interface IAuditBizService {

    /**
     * 处理请求
     *
     * @param auditDTO 审计信息
     */
    void handleRequest(AuditDTO auditDTO);

    /**
     * 处理响应
     *
     * @param auditDTO 审计信息
     */
    void handleResponse(AuditDTO auditDTO);

    /**
     * 保存审计信息
     *
     * @param auditDTO 审计信息
     */
    void saveAudit(AuditDTO auditDTO);
}
