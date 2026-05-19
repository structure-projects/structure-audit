package cn.structured.audit.biz.service;

import cn.structured.audit.domain.entity.Audit;

import java.util.List;

public interface IAuditEsService {

    void save(Audit audit);

    void saveAll(List<Audit> audits);

    void deleteById(Long id);

    Audit findById(Long id);

    List<Audit> searchByCondition(String action, String module, Integer status, String startTime, String endTime);
}
