package cn.structured.audit.biz.service;

import cn.structured.audit.domain.entity.Audit;

import java.util.List;

/**
 * 审计ES服务接口
 * 定义与Elasticsearch交互的审计数据操作
 *
 * @author structured
 * @since 2026-01-01
 */
public interface IAuditEsService {

    /**
     * 保存单个审计记录到ES
     *
     * @param audit 审计实体
     */
    void save(Audit audit);

    /**
     * 批量保存审计记录到ES
     *
     * @param audits 审计实体列表
     */
    void saveAll(List<Audit> audits);

    /**
     * 根据ID删除ES中的审计记录
     *
     * @param id 审计ID
     */
    void deleteById(Long id);

    /**
     * 根据ID从ES查询审计记录
     *
     * @param id 审计ID
     * @return 审计实体
     */
    Audit findById(Long id);

    /**
     * 根据条件从ES查询审计记录
     * 所有参数都为可选，为空则不限制该条件
     *
     * @param action 操作行为
     * @param module 操作模块
     * @param status 操作状态
     * @param startTime 开始时间（格式：yyyy-MM-dd HH:mm:ss）
     * @param endTime 结束时间（格式：yyyy-MM-dd HH:mm:ss）
     * @return 审计记录列表，按创建时间倒序排列
     */
    List<Audit> searchByCondition(String action, String module, Integer status, String startTime, String endTime);
}
