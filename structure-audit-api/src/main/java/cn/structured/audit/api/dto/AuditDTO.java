package cn.structured.audit.api.dto;


import lombok.Data;

/**
 * 审计日志
 *
 * @author chuck
 * @since 2026/1/2
 */
@Data
public class AuditDTO {

    /**
     * 组织ID（租户ID）
     * 用于多租户场景下的数据隔离
     */
    private Long organizationId;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 请求ID
     */
    private String requestId;

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
     * 耗时(毫秒)
     */
    private Long costTime;
}
