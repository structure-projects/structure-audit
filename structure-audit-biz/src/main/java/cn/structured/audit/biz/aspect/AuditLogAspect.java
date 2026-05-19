package cn.structured.audit.biz.aspect;

import cn.structure.common.constant.AuthConstant;
import cn.structured.audit.api.aop.AuditLog;
import cn.structured.audit.domain.entity.Audit;
import cn.structured.security.util.SecurityUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class AuditLogAspect {



    @Pointcut("@annotation(cn.structured.audit.api.aop.AuditLog)")
    public void operationLogPointcut() {
    }

    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Audit audit = new Audit();
        try {
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                audit.setIpAddress(request.getRemoteAddr());
                audit.setUserAgent(request.getHeader("User-Agent"));
            }

            // 获取注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            AuditLog auditLog = method.getAnnotation(AuditLog.class);
            if (auditLog != null) {
                audit.setAction(auditLog.value());
                // 这里可以根据需要将模块字符串转换为数字
                audit.setModule(auditLog.module());
            }

            // 获取方法参数
            Object[] args = joinPoint.getArgs();
            try {
                audit.setOperationParams(JSON.toJSONString(args));
            } catch (Exception e) {
                log.error("序列化参数失败", e);
                audit.setOperationParams("参数序列化失败");
            }

            // 执行方法
            Object result = joinPoint.proceed();

            // 记录成功信息
            audit.setStatus(1);
            try {
                audit.setOperationResult(JSON.toJSONString(result));
            } catch (Exception e) {
                log.error("序列化结果失败", e);
                audit.setOperationResult("结果序列化失败");
            }

            return result;
        } catch (Exception e) {
            // 记录失败信息
            audit.setStatus(0);
            audit.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            // 计算耗时
            long endTime = System.currentTimeMillis();
            audit.setCostTime(endTime - startTime);
            // 这里可以设置操作用户ID，需要从上下文中获取
             audit.setOperationId(getCurrentUserId());
            // 保存操作日志
            try {
//                operationRecordService.save(operationRecord);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }
    }

    // 这里需要实现获取当前用户ID的方法
    private String getCurrentUserId() {
        // 从上下文或会话中获取当前用户ID
        try {
            SecurityUtils.getUser();
            JSONObject user = JSON.parseObject(JSON.toJSONString(SecurityUtils.getUser()));
            return "" + ( null == user.getLong(AuthConstant.USER_ID) ? user.getLong("id") : user.getLong(AuthConstant.USER_ID));
        }catch (Exception  e) {
            log.warn("获取当前用户ID失败", e);
        }
        return null;
    }
}
