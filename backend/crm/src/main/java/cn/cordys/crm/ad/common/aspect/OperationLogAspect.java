package cn.cordys.crm.ad.common.aspect;

import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.common.annotation.OperationLog;
import cn.cordys.crm.ad.common.domain.AdOperationLog;
import cn.cordys.crm.ad.common.service.AdOperationLogService;
import cn.cordys.security.SessionUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 操作日志切面（V3.1 §4.5，L-18）。拦截 {@link OperationLog} 注解，方法成功后写入 {@code ad_operation_log}。
 * 写入失败不影响主流程。
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    @Autowired
    private AdOperationLogService operationLogService;

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @AfterReturning("@annotation(opLog)")
    public void record(JoinPoint joinPoint, OperationLog opLog) {
        try {
            AdOperationLog operationLog = new AdOperationLog();
            operationLog.setModule(opLog.module());
            operationLog.setBizType(opLog.bizType());
            operationLog.setAction(opLog.action());
            operationLog.setOperatorId(SessionUtils.getUserId());
            operationLog.setOrganizationId(OrganizationContext.getOrganizationId());
            operationLog.setTargetId(resolveTargetId(joinPoint, opLog.targetId()));
            operationLog.setIp(resolveIp());
            operationLogService.save(operationLog);
        } catch (Exception e) {
            log.warn("写入操作日志失败: {}", e.getMessage());
        }
    }

    private String resolveTargetId(JoinPoint joinPoint, String expression) {
        if (expression == null || expression.isEmpty()) {
            return null;
        }
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String[] params = discoverer.getParameterNames(method);
        if (params == null) {
            return null;
        }
        Object[] args = joinPoint.getArgs();
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < params.length; i++) {
            context.setVariable(params[i], args[i]);
        }
        try {
            Object value = parser.parseExpression(expression).getValue(context, Object.class);
            return value == null ? null : value.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}
