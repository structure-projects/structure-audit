package cn.structured.audit.biz.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "audit_history", createIndex = true)
public class AuditEs implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    private String requestId;

    private String operationId;

    private String operationAlias;

    private String action;

    private String module;

    private String operationParams;

    private String operationResult;

    private Integer status;

    private String errorMsg;

    private String ipAddress;

    private String userAgent;

    private Long costTime;

    private Long tenantId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;

    private Boolean deleted;
}
