package cn.structured.audit.domain.controller;

import cn.structure.common.entity.ResResultVO;
import cn.structure.common.utils.ResultUtilSimpleImpl;
import cn.structure.common.vo.ReqPage;
import cn.structure.common.vo.ResPage;
import cn.structured.audit.api.dto.AuditQuery;
import cn.structured.audit.api.vo.AuditVO;
import cn.structured.audit.domain.assembler.AuditAssembler;
import cn.structured.audit.domain.entity.Audit;
import cn.structured.audit.domain.service.IAuditService;
import cn.structured.mybatis.plus.starter.convert.ResPageConvert;
import cn.structured.mybatis.plus.starter.core.QueryJoinPageListWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "审计模块")
@RestController
@RequestMapping("/api/audit")
@AllArgsConstructor
public class AuditEndpoint {

    // 查询操作记录
    private final IAuditService auditService;

    @ApiOperation("查询操作记录")
    @GetMapping("/list")
    public ResResultVO<ResPage<AuditVO>> list(ReqPage reqPage, AuditQuery query) {
        Audit audit = new Audit();
        audit.setAction(query.getAction());
        audit.setModule(query.getModule());
        audit.setStatus(query.getStatus());
        QueryJoinPageListWrapper<Audit> wrapper = new QueryJoinPageListWrapper<>(audit);
        wrapper.setSearch(reqPage.getKeyword());
        wrapper.addSearch("ip_address");
        wrapper.addTime("update_time");
        wrapper.setBeginTime(query.getStartTime());
        wrapper.setEndTime(query.getEndTime());
        IPage<Audit> page = auditService.page(new Page<>(reqPage.getCurrentPage(), reqPage.getPageSize()), wrapper);
        // 查询操作人
        return ResultUtilSimpleImpl.success(ResPageConvert.convert(page, AuditAssembler::assembler));
    }
}
