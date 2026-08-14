package cn.cordys.crm.ad.order.job;

import cn.cordys.crm.ad.order.service.AdOrderService;
import cn.cordys.quartz.anno.QuartzScheduled;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 广告订单自动流转任务（M2，L-08 / 自动归档）。
 *
 * <p>职责：
 * <ul>
 *   <li>每分钟：将逾期的执行中订单(50，投放结束日已过期) 自动流转至结算中(80)。</li>
 *   <li>每分钟：扫描结算中(80)订单，满足「有关联合同 + 收款已收 + 付款已付」后自动归档(90)。</li>
 * </ul>
 * 具体扫描与流转逻辑在 {@link AdOrderService#checkOverdue()} / {@link AdOrderService#checkArchive()} 中。</p>
 */
@Slf4j
@Component
public class AdOrderOverdueJob {

    @Resource
    private AdOrderService adOrderService;

    /** 运行锁（防止并发重入）。 */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 逾期自动流转：执行中(50) 且投放结束日已过 → 结算中(80)。每分钟执行。
     */
    @QuartzScheduled(cron = "0 * * * * ?")
    public void overdueToSettlement() {
        if (!running.compareAndSet(false, true)) {
            log.warn("逾期检查已在运行中，跳过本次触发");
            return;
        }
        try {
            int count = adOrderService.checkOverdue();
            if (count > 0) {
                log.info("逾期自动流转完成，共 {} 个订单 50→80", count);
            }
        } catch (Exception e) {
            log.error("逾期自动流转执行失败", e);
        } finally {
            running.set(false);
        }
    }

    /**
     * 自动归档：结算中(80) 满足条件 → 已归档(90)。每分钟执行。
     */
    @QuartzScheduled(cron = "0 * * * * ?")
    public void autoArchive() {
        try {
            int count = adOrderService.checkArchive();
            if (count > 0) {
                log.info("自动归档完成，共 {} 个订单 80→90", count);
            }
        } catch (Exception e) {
            log.error("自动归档执行失败", e);
        }
    }

    /**
     * 触发一次逾期检查（保留被动调用入口，供手动/调试使用）。幂等。
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
