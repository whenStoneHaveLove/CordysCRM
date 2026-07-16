package cn.cordys.crm.ad.common.constants;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 纯逻辑单测（NO @SpringBootTest）：验证订单主状态机转换规则。
 *
 * <p>覆盖：主链路 0→10→20→50→70 与作废(→100)/归档(80→90)；
 * L-14 审批开关（关闭时 0→20 直达）；L-21 驳回不在此类守卫（仅回退状态）。</p>
 */
class OrderStateMachineTest {

    /* ---------- 合法转换 ---------- */

    @Test
    void allowed_mainChain() {
        assertTrue(OrderStateMachine.canTransit(OrderStateMachine.DRAFT, OrderStateMachine.PENDING_BOSS_APPROVAL));
        assertTrue(OrderStateMachine.canTransit(OrderStateMachine.PENDING_BOSS_APPROVAL, OrderStateMachine.APPROVED));
        assertTrue(OrderStateMachine.canTransit(OrderStateMachine.APPROVED, OrderStateMachine.EXECUTING));
        assertTrue(OrderStateMachine.canTransit(OrderStateMachine.EXECUTING, OrderStateMachine.EXECUTION_COMPLETED));
    }

    @Test
    void allowed_voidFromAllStates() {
        int[] froms = {
                OrderStateMachine.DRAFT,
                OrderStateMachine.PENDING_BOSS_APPROVAL,
                OrderStateMachine.EXECUTING,
                OrderStateMachine.CHANGE_APPROVING,
                OrderStateMachine.EXECUTION_COMPLETED,
                OrderStateMachine.SETTLEMENT
        };
        for (int from : froms) {
            assertTrue(OrderStateMachine.canTransit(from, OrderStateMachine.VOIDED), "from=" + from + " 应可作废");
        }
    }

    @Test
    void allowed_archiveFromSettlement() {
        assertTrue(OrderStateMachine.canTransit(OrderStateMachine.SETTLEMENT, OrderStateMachine.ARCHIVED));
    }

    /* ---------- 非法转换 ---------- */

    @Test
    void rejected_illegalJumps() {
        assertFalse(OrderStateMachine.canTransit(OrderStateMachine.DRAFT, OrderStateMachine.EXECUTING), "草稿不可直达执行中");
        assertFalse(OrderStateMachine.canTransit(OrderStateMachine.EXECUTION_COMPLETED, OrderStateMachine.PENDING_BOSS_APPROVAL), "执行完成不可回退待审");
        assertFalse(OrderStateMachine.canTransit(OrderStateMachine.APPROVED, OrderStateMachine.VOIDED), "审核通过不可直接作废");
    }

    @Test
    void rejected_sameState() {
        assertFalse(OrderStateMachine.canTransit(OrderStateMachine.DRAFT, OrderStateMachine.DRAFT));
        assertFalse(OrderStateMachine.canTransit(OrderStateMachine.EXECUTING, OrderStateMachine.EXECUTING));
    }

    /* ---------- L-14 审批开关 ---------- */

    @Test
    void l14_approvalDisabledDraftDirectToApproved() {
        assertTrue(OrderStateMachine.canTransit(OrderStateMachine.DRAFT, OrderStateMachine.APPROVED, false),
                "审批关闭：草稿提交直达审核通过");
        assertFalse(OrderStateMachine.canTransit(OrderStateMachine.DRAFT, OrderStateMachine.APPROVED, true),
                "审批开启：草稿不可直达审核通过");
    }

    /* ---------- 辅助查询 ---------- */

    @Test
    void nextStates_and_role() {
        List<Integer> fromDraft = OrderStateMachine.nextStates(OrderStateMachine.DRAFT);
        assertTrue(fromDraft.contains(OrderStateMachine.PENDING_BOSS_APPROVAL));
        assertTrue(fromDraft.contains(OrderStateMachine.VOIDED));

        assertEquals(OrderStateMachine.ROLE_BOSS,
                OrderStateMachine.requiredRoleFor(OrderStateMachine.PENDING_BOSS_APPROVAL, OrderStateMachine.APPROVED));
        assertNull(OrderStateMachine.requiredRoleFor(OrderStateMachine.DRAFT, OrderStateMachine.EXECUTING));
    }
}
