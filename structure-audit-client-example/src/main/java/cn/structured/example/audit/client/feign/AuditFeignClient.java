package cn.structured.example.audit.client.feign;

import cn.structured.audit.api.dto.AuditDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "audit-center", url = "${audit.server.url}")
public interface AuditFeignClient {

    @PostMapping("/open-api/audit/add")
    void addAudit(@RequestBody AuditDTO auditDTO);
}
