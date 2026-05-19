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

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("request_id")
    private String requestId;

    @TableField("operation_id")
    private String operationId;

    @TableField("operation_alias")
    private String operationAlias;

    @TableField("action")
    private String action;

    @TableField("module")
    private String module;

    @TableField("operation_params")
    private String operationParams;

    @TableField("operation_result")
    private String operationResult;

    @TableField("status")
    private Integer status;

    @TableField("error_msg")
    private String errorMsg;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField("cost_time")
    private Long costTime;

    @TableField("organization_id")
    private Long tenantId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Boolean deleted;

}
