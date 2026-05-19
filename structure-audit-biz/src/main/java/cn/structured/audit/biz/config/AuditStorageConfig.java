package cn.structured.audit.biz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 审计存储配置
 * 用于配置审计数据的存储策略
 * 配置前缀: structure.audit.storage
 *
 * @author structured
 * @since 2026-05-19
 */
@Data
@Component
@ConfigurationProperties(prefix = "structure.audit.storage")
public class AuditStorageConfig {

    /**
     * 默认存储策略
     * - MYSQL: 仅存储到MySQL
     * - ELASTICSEARCH: 仅存储到Elasticsearch
     * - BOTH: 同时存储到MySQL和Elasticsearch
     */
    private StorageType type = StorageType.MYSQL;

    /**
     * 存储策略枚举
     */
    public enum StorageType {
        /**
         * 仅存储到MySQL数据库
         * 适用于传统审计场景，数据量不大的情况
         */
        MYSQL,
        /**
         * 仅存储到Elasticsearch
         * 适用于高并发场景，直接使用ES做查询
         */
        ELASTICSEARCH,
        /**
         * 同时存储到MySQL和Elasticsearch
         * 适用于需要实时查询和历史归档的场景
         */
        BOTH
    }
}
