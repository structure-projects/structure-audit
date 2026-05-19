package cn.structured.example.audit.client.service;

import cn.structured.audit.api.dto.AuditDTO;
import cn.structured.audit.api.service.IRemoteAuditService;
import cn.structured.example.audit.client.feign.AuditFeignClient;

import javax.annotation.Resource;

/**
 * 远程审计服务实现类, 用于调用审计服务。 服务端定义了接口客户端这么可以选用自己合适的方式进行实现
 *
 */
public class AuditRemoteServiceImpl implements IRemoteAuditService {

    @Resource
    private AuditFeignClient auditFeignClient;

    @Override
    public void saveAuditRecord(AuditDTO audit) {
        auditFeignClient.addAudit(audit);
    }
}
