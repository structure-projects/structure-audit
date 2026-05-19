package cn.structured.audit.biz.controller.open;

import cn.structure.common.entity.ResResultVO;
import cn.structure.common.utils.ResultUtilSimpleImpl;
import cn.structured.audit.api.dto.AuditDTO;
import cn.structured.audit.biz.service.IAuditBizService;
import cn.structured.audit.domain.service.IAuditService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "审计模块")
@RestController
@RequestMapping("/open-api/audit")
@AllArgsConstructor
public class OpenAuditController {

    // 查询操作记录
    private final IAuditBizService auditBizService;

    @ApiOperation("添加操作日志")
    @PostMapping("/add")
    public ResResultVO<Void> add(@RequestBody AuditDTO auditDTO) {
        auditBizService.saveAudit(auditDTO);
        return ResultUtilSimpleImpl.success(null);
    }
}
