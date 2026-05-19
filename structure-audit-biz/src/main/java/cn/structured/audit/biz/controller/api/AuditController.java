package cn.structured.audit.biz.controller.api;

import cn.structure.common.entity.ResResultVO;
import cn.structure.common.utils.ResultUtilSimpleImpl;
import cn.structure.common.vo.ReqPage;
import cn.structure.common.vo.ResPage;
import cn.structured.audit.api.dto.AuditDTO;
import cn.structured.audit.api.dto.AuditQuery;
import cn.structured.audit.api.vo.AuditVO;
import cn.structured.audit.biz.service.IAuditBizService;
import cn.structured.audit.biz.service.IAuditEsService;
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
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "审计模块")
@RestController
@RequestMapping("/api/audit")
@AllArgsConstructor
public class AuditController {

    private final IAuditService auditService;
    private final IAuditEsService auditEsService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @ApiOperation("查询历史操作记录(ES)")
    @GetMapping("/history/list")
    public ResResultVO<List<AuditVO>> historyList(AuditQuery query) {
        String startTime = query.getStartTime() != null ? query.getStartTime().format(FORMATTER) : null;
        String endTime = query.getEndTime() != null ? query.getEndTime().format(FORMATTER) : null;
        
        List<Audit> audits = auditEsService.searchByCondition(
                query.getAction(),
                query.getModule(),
                query.getStatus(),
                startTime,
                endTime
        );
        
        List<AuditVO> voList = audits.stream()
                .map(AuditAssembler::assembler)
                .collect(Collectors.toList());
        
        return ResultUtilSimpleImpl.success(voList);
    }
}
