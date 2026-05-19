package cn.structured.example.audit.client.aspect;

import cn.structured.audit.api.aop.AuditLog;
import cn.structured.audit.api.dto.AuditDTO;
import cn.structured.audit.domain.assembler.AuditAssembler;
import cn.structured.audit.domain.entity.Audit;
import cn.structured.audit.domain.service.IAuditService;
import cn.structured.example.audit.client.feign.AuditFeignClient;
import com.alibaba.fastjson.JSON;
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
import java.util.UUID;

@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class AuditLogAspect {

    // 审计日志服务 -> 使用domain 的基础实现作为存储
    private final IAuditService auditService;

    @Pointcut("@annotation(cn.structured.audit.api.aop.AuditLog)")
    public void auditLogPointcut() {
    }

    @Around("auditLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        HttpServletRequest request = null;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            request = attributes.getRequest();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditLog auditLog = method.getAnnotation(AuditLog.class);

        String action = auditLog.value();
        String module = auditLog.module();

        Object[] args = joinPoint.getArgs();
        String operationParams = null;
        try {
            operationParams = JSON.toJSONString(args);
        } catch (Exception e) {
            log.warn("参数序列化失败", e);
        }

        String operatorId = getOperatorId(request);
        String operatorName = getOperatorName(request);
        String ipAddress = getClientIp(request);
        String userAgent = request != null ? request.getHeader("User-Agent") : null;

        Object result = null;
        Integer status = 1;
        String errorMsg = null;

        try {
            result = joinPoint.proceed();
            status = 1;
            log.debug("方法执行成功, action: {}", action);
            return result;
        } catch (Exception e) {
            status = 0;
            errorMsg = e.getMessage();
            log.error("方法执行失败, action: {}", action, e);
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            recordAuditLogAsync(requestId, action, module, operatorId, operatorName,
                    operationParams, result, status, errorMsg, ipAddress, userAgent, costTime);
        }
    }

    private void recordAuditLogAsync(String requestId, String action, String module,
                                     String operatorId, String operatorName, String operationParams,
                                     Object result, Integer status, String errorMsg,
                                     String ipAddress, String userAgent, Long costTime) {
        try {
            AuditDTO auditDTO = new AuditDTO();
            auditDTO.setRequestId(requestId);
            auditDTO.setOperatorId(operatorId);
            auditDTO.setOperatorName(operatorName);
            auditDTO.setAction(action);
            auditDTO.setModule(module);
            auditDTO.setOperationParams(operationParams);
            auditDTO.setOperationResult(result != null ? JSON.toJSONString(result) : null);
            auditDTO.setStatus(status);
            auditDTO.setErrorMsg(errorMsg);
            auditDTO.setIpAddress(ipAddress);
            auditDTO.setUserAgent(userAgent);
            auditDTO.setCostTime(costTime);

            Audit assembler = AuditAssembler.assembler(auditDTO);

            auditService.save(assembler);
            log.info("注解方式审计日志记录成功, action: {}, requestId: {}", action, requestId);
        } catch (Exception e) {
            log.error("注解方式审计日志记录失败, action: {}", action, e);
        }
    }

    private String getOperatorId(HttpServletRequest request) {
        if (request == null) {
            return "system";
        }
        String operatorId = request.getHeader("X-Operator-Id");
        if (operatorId == null || operatorId.isEmpty()) {
            operatorId = request.getHeader("X-User-Id");
        }
        return operatorId != null ? operatorId : "system";
    }

    private String getOperatorName(HttpServletRequest request) {
        if (request == null) {
            return "系统";
        }
        String operatorName = request.getHeader("X-Operator-Name");
        if (operatorName == null || operatorName.isEmpty()) {
            operatorName = request.getHeader("X-User-Name");
        }
        return operatorName != null ? operatorName : "系统";
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
