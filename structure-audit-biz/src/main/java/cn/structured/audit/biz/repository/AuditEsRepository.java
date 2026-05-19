package cn.structured.audit.biz.repository;

import cn.structured.audit.biz.entity.AuditEs;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEsRepository extends ElasticsearchRepository<AuditEs, Long> {

    List<AuditEs> findByAction(String action);

    List<AuditEs> findByModule(String module);

    List<AuditEs> findByStatus(Integer status);

    List<AuditEs> findByActionAndModule(String action, String module);
}
