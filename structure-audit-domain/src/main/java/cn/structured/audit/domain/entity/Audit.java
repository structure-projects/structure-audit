package cn.structured.audit.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 操作日志表
 * </p>
 *
 * @author chuck
 * @since 2026-01-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("audit")
public class Audit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 请求ID
     */
    @TableField("request_id")
    private String requestId;

    /**
     * 操作人
     */
    @TableField("operation_id")
    private String operationId;

    /**
     * 操作人别名
     */
    @TableField("operation_alias")
    private String operationAlias;

    /**
     * 操作行为
     */
    @TableField("action")
    private String action;

    /**
     * 操作模块
     */
    @TableField("module")
    private String module;

    /**
     * 操作参数
     */
    @TableField("operation_params")
    private String operationParams;

    /**
     * 操作结果
     */
    @TableField("operation_result")
    private String operationResult;

    /**
     * 操作状态：1成功 0失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 错误信息
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 操作IP
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 耗时(毫秒)
     */
    @TableField("cost_time")
    private Long costTime;

    /**
     * 租户ID
     */
    @TableField("organization_id")
    private Long tenantId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 修改人
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 逻辑删除 0-否 1-是
     */
    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Boolean deleted;

}
