package cn.cordys.crm.ad.order.job;

import cn.cordys.crm.ad.order.service.AdOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 广告订单逾期自动流转任务（M2，L-08）。
 *
 * <p>职责：将逾期的执行中订单(50，投放结束日已过期) 自动流转至结算中(80)。
 * 具体扫描与流转逻辑在 {@link AdOrderService#checkOverdue()} 中，本类仅作为触发入口。</p>
 *
 * <p> guarded：内置运行锁，避免重复触发造成并发重入；当前为被动调用入口，
 * 未注册 Quartz/定时调度（由调度平台或后续 M 模块按需触发 {@link #triggerOverdueCheck()}）。</p>
 */
@Slf4j
@Component
public class AdOrderOverdueJob {

    @Resource
    private AdOrderService adOrderService;

    /** 运行锁（防止并发重入）。 */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 触发一次逾期检查。幂等：若正在运行则直接跳过。
     *
     * @return 本次流转的订单数
     */
    public int triggerOverdueCheck() {
        if (!running.compareAndSet(false, true)) {
            log.warn("逾期检查已在运行中，跳过本次触发");
            return 0;
        }
        try {
            return adOrderService.checkOverdue();
        } catch (Exception e) {
            log.error("逾期自动流转执行失败", e);
            return 0;
        } finally {
            running.set(false);
        }
    }
}
