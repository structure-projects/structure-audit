package cn.structured.example.audit.client.config.domain;

import cn.structure.starter.tenant.TenantContextHolder;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * mysql 装配
 *
 * @author cqliut
 * @version 2023.0704
 * @since 1.0.1
 */
@Slf4j
@Configuration
@MapperScan("cn.structured.audit.domain.mapper")
public class DomainMyBatisConfiguration {

    private static final String TENANT_ID = "organization_id";

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                // 默认租户为1
                try {
                    String tenantId = TenantContextHolder.getTenantId();
                    return new LongValue(tenantId);

                } catch (Exception e) {
                    log.error("获取租户ID失败：{}", e.getMessage());
                    return new LongValue(1);
                } finally {
                    TenantContextHolder.clear();
                }
            }

            @Override
            public String getTenantIdColumn() {
                return TENANT_ID;
            }

            // 这是 default 方法,默认返回 false 表示所有表都需要拼多租户条件
            @Override
            public boolean ignoreTable(String tableName) {
                TableInfo tableInfo = TableInfoHelper.getTableInfos().
                        stream()
                        .filter(table -> table.getTableName().equals(tableName))
                        .findFirst().orElse(null);
                if (null == tableInfo) {
                    return true;
                }
                TableFieldInfo tableField = tableInfo.getFieldList().stream().filter(tableFieldInfo ->
                        tableFieldInfo.getColumn().equals(TENANT_ID)
                ).findFirst().orElse(null);
                return (null == tableField);
            }
        }));
        return interceptor;
    }

}
