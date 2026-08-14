package cn.cordys.crm.ad.common.aspect;

import cn.cordys.common.uid.IDGenerator;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.common.annotation.OperationLog;
import cn.cordys.crm.ad.common.domain.AdOperationLog;
import cn.cordys.crm.ad.common.service.AdOperationLogService;
import cn.cordys.mybatis.BaseMapper;
import cn.cordys.security.SessionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 操作日志切面（V3.1 §4.5，L-18/L-30）。拦截 {@link OperationLog} 注解，
 * 方法成功后写入 {@code ad_operation_log}，包含变更前后的值。
 * 写入失败不影响主流程。
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    @Autowired
    private AdOperationLogService operationLogService;

    @Autowired
    private ApplicationContext applicationContext;

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Around("@annotation(opLog)")
    public Object record(ProceedingJoinPoint joinPoint, OperationLog opLog) throws Throwable {
        // 1. 方法执行前：尝试查询旧数据
        String targetId = resolveTargetId(joinPoint, opLog.targetId());
        Map<String, Object> beforeMap = queryBeforeData(opLog.module(), targetId);

        // 2. 执行目标方法
        Object result = joinPoint.proceed();

        // 3. 方法成功后：写入操作日志（含 before/after）
        try {
            // targetId 为空时（如 CREATE），尝试从返回值提取 id
            String finalTargetId = targetId;
            if (finalTargetId == null || finalTargetId.isBlank()) {
                finalTargetId = extractIdFromResult(result);
            }

            AdOperationLog operationLog = new AdOperationLog();
            operationLog.setId(IDGenerator.nextStr());
            operationLog.setModule(opLog.module());
            operationLog.setBizType(opLog.bizType());
            operationLog.setAction(opLog.action());
            operationLog.setOperatorId(SessionUtils.getUserId());
            operationLog.setOrganizationId(OrganizationContext.getOrganizationId());
            operationLog.setTargetId(finalTargetId);
            operationLog.setIp(resolveIp());
            operationLog.setCreateTime(System.currentTimeMillis());

            // 变更前后值
            if (finalTargetId != null) {
                Map<String, Object> afterMap = queryAfterData(opLog.module(), finalTargetId);
                if (afterMap != null && !afterMap.isEmpty()) {
                    if (beforeMap != null && !beforeMap.isEmpty()) {
                        // UPDATE：记录 before + after
                        operationLog.setBeforeValue(toJson(beforeMap));
                    }
                    // CREATE / UPDATE 都记录 after
                    operationLog.setAfterValue(toJson(afterMap));
                }
            }

            operationLogService.save(operationLog);
        } catch (Exception e) {
            log.warn("写入操作日志失败: {}", e.getMessage());
        }

        return result;
    }

    /**
     * 从方法返回值中提取 id（用于 CREATE 等 targetId 为空的操作）。
     */
    private String extractIdFromResult(Object result) {
        if (result == null) {
            return null;
        }
        try {
            Field idField = result.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object idValue = idField.get(result);
            return idValue == null ? null : idValue.toString();
        } catch (NoSuchFieldException e) {
            // 尝试父类
            try {
                Field idField = result.getClass().getSuperclass().getDeclaredField("id");
                idField.setAccessible(true);
                Object idValue = idField.get(result);
                return idValue == null ? null : idValue.toString();
            } catch (Exception ex) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 方法执行前，通过 targetId 查询旧数据。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> queryBeforeData(String module, String targetId) {
        if (targetId == null || targetId.isBlank()) {
            return null;
        }
        BaseMapper<Object> mapper = resolveMapper(module);
        if (mapper == null) {
            return null;
        }
        try {
            Object entity = mapper.selectByPrimaryKey(targetId);
            if (entity == null) {
                return null;
            }
            return entityToMap(entity);
        } catch (Exception e) {
            log.debug("查询操作日志变更前数据失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 方法执行后，通过 targetId 查询新数据。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> queryAfterData(String module, String targetId) {
        if (targetId == null || targetId.isBlank()) {
            return null;
        }
        BaseMapper<Object> mapper = resolveMapper(module);
        if (mapper == null) {
            return null;
        }
        try {
            Object entity = mapper.selectByPrimaryKey(targetId);
            if (entity == null) {
                return null;
            }
            return entityToMap(entity);
        } catch (Exception e) {
            log.debug("查询操作日志变更后数据失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 根据 module 名称查找对应的 BaseMapper Bean。
     */
    @SuppressWarnings("rawtypes")
    private BaseMapper resolveMapper(String module) {
        try {
            String mapperBeanName = moduleToMapperBean(module);
            return applicationContext.getBean(mapperBeanName, BaseMapper.class);
        } catch (Exception e) {
            log.debug("未找到模块 {} 对应的 Mapper: {}", module, e.getMessage());
            return null;
        }
    }

    /**
     * module -> Mapper bean 名称映射。
     * 约定：module 如 AD_ORDER 对应 extAdOrderMapper（首字母小写）。
     */
    private String moduleToMapperBean(String module) {
        // AD_ORDER -> ExtAdOrderMapper -> extAdOrderMapper
        String[] parts = module.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (i == 0) {
                sb.append(part);
            } else {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    sb.append(part.substring(1));
                }
            }
        }
        return "ext" + Character.toUpperCase(sb.charAt(0)) + sb.substring(1) + "Mapper";
    }

    /**
     * 实体对象转 Map（只取基本类型字段，忽略复杂对象）。
     */
    private Map<String, Object> entityToMap(Object entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        Class<?> clazz = entity.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    if (value != null && isSimpleType(value.getClass())) {
                        map.put(field.getName(), value);
                    }
                } catch (Exception ignored) {
                    // skip
                }
            }
            clazz = clazz.getSuperclass();
        }
        return map;
    }

    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || Number.class.isAssignableFrom(type)
                || type == Boolean.class
                || type == java.util.Date.class
                || type == java.sql.Date.class
                || type == java.sql.Timestamp.class
                || type == java.time.LocalDate.class
                || type == java.time.LocalDateTime.class;
    }

    /**
     * 计算变更 diff：比较 before 和 after，返回有变化的字段。
     */
    private Map<String, Object> computeDiff(Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> diff = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : after.entrySet()) {
            String key = entry.getKey();
            Object newVal = entry.getValue();
            Object oldVal = before.get(key);
            if (!java.util.Objects.equals(oldVal, newVal)) {
                diff.put(key, newVal);
            }
        }
        return diff;
    }

    /**
     * Map 转合法 JSON 字符串。
     */
    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String resolveTargetId(ProceedingJoinPoint joinPoint, String expression) {
        if (expression == null || expression.isEmpty()) {
            return null;
        }
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String[] params = discoverer.getParameterNames(method);
        if (params == null) {
            return null;
        }
        Object[] args = joinPoint.getArgs();
        StandardEvaluationContext context = new StandardEvaluationContext();
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
