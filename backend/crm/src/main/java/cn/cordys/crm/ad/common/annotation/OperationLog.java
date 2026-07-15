package cn.cordys.crm.ad.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解（V3.1 §4.5，L-18/L-30）。
 *
 * <p>在需留痕的方法上标注，切面写入 {@code ad_operation_log}（仅存变更字段，L-30）。</p>
 *
 * <p>示例：
 * <pre>
 *   &#64;OperationLog(module = "ORDER", action = "STATUS_CHANGE", targetId = "#id")
 * </pre>
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /** 模块(ORDER/CONTRACT/PAYMENT/SYSTEM...)。 */
    String module();

    /** 动作(STATUS_CHANGE/CREATE/UPDATE/VOID...)。 */
    String action();

    /** 业务类型（可选）。 */
    String bizType() default "";

    /** 目标 id 的 SpEL 表达式（如 "#id"、"#{request.id}"），可选。 */
    String targetId() default "";
}
