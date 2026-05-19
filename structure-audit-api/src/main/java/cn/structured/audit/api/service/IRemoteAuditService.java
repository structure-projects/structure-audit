package cn.structured.audit.api.service;


import cn.structured.audit.api.dto.AuditDTO;

/**
 * 远程审计服务
 *
 * @author chuck
 * @since 2021/12/23
 */
public interface IRemoteAuditService {


    /**
     * 保存操作记录
     *
     * @param audit 操作记录
     */
    void saveOperationRecord(AuditDTO audit);
}
