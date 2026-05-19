package cn.structured.example.audit.client.config.domain;

import cn.structure.common.constant.AuthConstant;
import cn.structured.security.util.SecurityUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * @author cqliut
 * @version 2022.0726
 * @since 1.0.1
 */
@Slf4j
@Configuration
public class DomainMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 检查字段是否已存在值，如果不存在则填充默认值
        if (this.getFieldValByName("delFlag", metaObject) == null) {
            this.setFieldValByName("delFlag", Boolean.FALSE, metaObject);
        }
        if (this.getFieldValByName("enabled", metaObject) == null) {
            this.setFieldValByName("enabled", Boolean.TRUE, metaObject);
        }
        if (this.getFieldValByName("deleted", metaObject) == null) {
            this.setFieldValByName("deleted", Boolean.FALSE, metaObject);
        }
        if (this.getFieldValByName("createDate", metaObject) == null) {
            this.setFieldValByName("createDate", LocalDateTime.now(), metaObject);
        }
        if (this.getFieldValByName("createTime", metaObject) == null) {
            this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
        }
        if (this.getFieldValByName("createBy", metaObject) == null) {
            this.setFieldValByName("createBy", getUserId(), metaObject);
        }
        if (this.getFieldValByName("updateDate", metaObject) == null) {
            this.setFieldValByName("updateDate", LocalDateTime.now(), metaObject);
        }
        if (this.getFieldValByName("updateTime", metaObject) == null) {
            this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        }
        if (this.getFieldValByName("updateBy", metaObject) == null) {
            this.setFieldValByName("updateBy", getUserId(), metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 检查字段是否已存在值，如果不存在则更新为新值
        if ( this.getFieldValByName("updateDate", metaObject) == null) {
            this.setFieldValByName("updateDate", LocalDateTime.now(), metaObject);
        }
        if (this.getFieldValByName("updateTime", metaObject) == null) {
            this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        }
        if (this.getFieldValByName("updateBy", metaObject) == null) {
            this.setFieldValByName("updateBy", getUserId(), metaObject);
        }
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    private Object getUserId() {
        try {
            SecurityUtils.getUser();
            JSONObject user = JSON.parseObject(JSON.toJSONString(SecurityUtils.getUser()));
            return null == user.getLong(AuthConstant.USER_ID) ? user.getLong("id") : user.getLong(AuthConstant.USER_ID);
        } catch (Exception e) {
            log.debug("get user id is error -> message = {}", e.getMessage());
        }
        return null;
    }
}
