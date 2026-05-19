package cn.structured.audit.biz.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计ES实体类
 * 对应Elasticsearch中的audit_history索引
 * 用于存储历史审计数据
 *
 * @author structured
 * @since 2026-01-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "audit_history", createIndex = true)
public class AuditEs implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id
    private Long id;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 操作人ID
     */
    private String operationId;

    /**
     * 操作人别名
     */
    private String operationAlias;

    /**
     * 操作行为
     */
    private String action;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作参数
     */
    private String operationParams;

    /**
     * 操作结果
     */
    private String operationResult;

    /**
     * 操作状态：1成功 0失败
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 操作IP
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 耗时（毫秒）
     */
    private Long costTime;

    /**
     * 租户ID（组织ID）
     */
    private Long tenantId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 更新人
     */
    private Long updateBy;

    /**
     * 逻辑删除标识
     */
    private Boolean deleted;
}
