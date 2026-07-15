package cn.cordys.crm.ad.order.service;

import cn.cordys.crm.ad.common.constants.OrderStateMachine;
import cn.cordys.crm.ad.order.domain.AdOrder;
import org.springframework.stereotype.Service;

/**
 * 广告订单服务（M1 骨架，T-06）。
 *
 * <p>订单状态流转由 {@link OrderStateMachine} 驱动，<b>不依赖</b> {@code cn.cordys.crm.approval} 模块。
 * M1 仅提供状态机校验/应用的最小封装；完整 CRUD、编号生成、财务前置等在 M2 落地。</p>
 */
@Service
public class AdOrderService {

    /**
     * 审批开关开启时是否允许从当前状态转换到目标状态。
     */
    public boolean canTransit(AdOrder order, int to) {
        return OrderStateMachine.canTransit(order.getStatus(), to);
    }

    /**
     * 是否允许状态转换（L-14 审批开关可关闭，关闭时提交 0→20 直达）。
     */
    public boolean canTransit(AdOrder order, int to, boolean approvalEnabled) {
        return OrderStateMachine.canTransit(order.getStatus(), to, approvalEnabled);
    }

    /**
     * 应用状态转换：校验通过后更新订单主状态。返回是否成功。
     */
    public boolean applyTransit(AdOrder order, int to, boolean approvalEnabled) {
        if (!OrderStateMachine.canTransit(order.getStatus(), to, approvalEnabled)) {
            return false;
        }
        order.setStatus(to);
        return true;
    }

    /**
     * 返回当前订单状态下所有可达的下一状态。
     */
    public java.util.List<Integer> nextStates(AdOrder order) {
        return OrderStateMachine.nextStates(order.getStatus());
    }
}
