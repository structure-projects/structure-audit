package cn.structured.audit.domain.service.impl;

import cn.structured.audit.domain.entity.Audit;
import cn.structured.audit.domain.mapper.AuditMapper;
import cn.structured.audit.domain.service.IAuditService;
import cn.structured.mybatis.plus.starter.base.BaseServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AuditServiceImpl extends BaseServiceImpl<AuditMapper, Audit> implements IAuditService {

}
