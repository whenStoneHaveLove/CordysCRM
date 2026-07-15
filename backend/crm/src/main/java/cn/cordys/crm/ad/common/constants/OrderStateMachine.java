package cn.cordys.crm.ad.common.constants;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单主状态机（V3.1 §6.1/§6.2，配置化方式 A）。
 *
 * <p>集中维护 {@code (from, to, trigger, requiredRole)} 映射，业务代码统一调用
 * {@link #canTransit(int, int)} / {@link #nextStates(int)} / {@link #requiredRoleFor(int, int)}，
 * <b>禁止</b>在业务逻辑中硬编码状态跳变。</p>
 *
 * <p>状态码采用 10 倍数间隔编码（0/10/20/.../100），预留后续插入新状态。</p>
 *
 * <p>关键规则：
 * <ul>
 *   <li>L-14 审批开关：开关关闭时提交(0)直达审核通过(20)，见 {@link #canTransit(int, int, boolean)}。</li>
 *   <li>L-21 驳回不删附件：驳回(10→0)仅回退状态，附件保留（由业务服务处理，本类仅描述转换）。</li>
 * </ul>
 * </p>
 */
@Slf4j
public final class OrderStateMachine {

    private OrderStateMachine() {
    }

    /* ---------- 主状态码（10 倍数编码） ---------- */
    public static final int DRAFT = 0;                    // 草稿
    public static final int PENDING_BOSS_APPROVAL = 10;  // 待老板审核
    public static final int APPROVED = 20;               // 审核通过
    public static final int PENDING_PREPAY_CONFIRM = 30; // 待确认预收款
    public static final int PENDING_MEDIA_PREPAY = 40;   // 待付媒体预付款
    public static final int EXECUTING = 50;              // 执行中
    public static final int CHANGE_APPROVING = 60;       // 变更审核中
    public static final int EXECUTION_COMPLETED = 70;    // 执行完成
    public static final int SETTLEMENT = 80;             // 结算中
    public static final int ARCHIVED = 90;               // 已归档
    public static final int VOIDED = 100;                // 已作废

    /* ---------- 角色（requiredRole） ---------- */
    public static final String ROLE_MEDIA = "ROLE_MEDIA";
    public static final String ROLE_FINANCE = "ROLE_FINANCE";
    public static final String ROLE_BOSS = "ROLE_BOSS";
    public static final String ROLE_SYSTEM = "SYSTEM";

    /* ---------- 触发动作（trigger） ---------- */
    public static final String TRIGGER_SUBMIT = "SUBMIT";
    public static final String TRIGGER_REJECT = "REJECT";
    public static final String TRIGGER_APPROVE = "APPROVE";
    public static final String TRIGGER_AUTO_PREPAY = "AUTO_PREPAY";
    public static final String TRIGGER_AUTO_MEDIA_PREPAY = "AUTO_MEDIA_PREPAY";
    public static final String TRIGGER_AUTO_EXECUTE = "AUTO_EXECUTE";
    public static final String TRIGGER_PREPAY_CONFIRMED_MEDIA = "PREPAY_CONFIRMED_MEDIA";
    public static final String TRIGGER_PREPAY_CONFIRMED_EXECUTE = "PREPAY_CONFIRMED_EXECUTE";
    public static final String TRIGGER_MEDIA_PREPAY_PAID = "MEDIA_PREPAY_PAID";
    public static final String TRIGGER_APPLY_CHANGE = "APPLY_CHANGE";
    public static final String TRIGGER_CHANGE_APPROVED = "CHANGE_APPROVED";
    public static final String TRIGGER_CHANGE_REJECTED = "CHANGE_REJECTED";
    public static final String TRIGGER_COMPLETE_EXECUTION = "COMPLETE_EXECUTION";
    public static final String TRIGGER_ENTER_SETTLEMENT = "ENTER_SETTLEMENT";
    public static final String TRIGGER_SETTLEMENT_UPDATE = "SETTLEMENT_UPDATE";
    public static final String TRIGGER_ARCHIVE = "ARCHIVE";
    public static final String TRIGGER_FORCE_ARCHIVE = "FORCE_ARCHIVE";
    public static final String TRIGGER_VOID = "VOID";

    /** 状态转换定义。 */
    private record Transition(int from, int to, String trigger, String requiredRole) {
    }

    private static final List<Transition> TRANSITIONS = List.of(
            new Transition(DRAFT, PENDING_BOSS_APPROVAL, TRIGGER_SUBMIT, ROLE_MEDIA),
            new Transition(PENDING_BOSS_APPROVAL, DRAFT, TRIGGER_REJECT, ROLE_BOSS),
            new Transition(PENDING_BOSS_APPROVAL, APPROVED, TRIGGER_APPROVE, ROLE_BOSS),
            new Transition(APPROVED, PENDING_PREPAY_CONFIRM, TRIGGER_AUTO_PREPAY, ROLE_SYSTEM),
            new Transition(APPROVED, PENDING_MEDIA_PREPAY, TRIGGER_AUTO_MEDIA_PREPAY, ROLE_SYSTEM),
            new Transition(APPROVED, EXECUTING, TRIGGER_AUTO_EXECUTE, ROLE_SYSTEM),
            new Transition(PENDING_PREPAY_CONFIRM, PENDING_MEDIA_PREPAY, TRIGGER_PREPAY_CONFIRMED_MEDIA, ROLE_SYSTEM),
            new Transition(PENDING_PREPAY_CONFIRM, EXECUTING, TRIGGER_PREPAY_CONFIRMED_EXECUTE, ROLE_SYSTEM),
            new Transition(PENDING_MEDIA_PREPAY, EXECUTING, TRIGGER_MEDIA_PREPAY_PAID, ROLE_SYSTEM),
            new Transition(EXECUTING, CHANGE_APPROVING, TRIGGER_APPLY_CHANGE, ROLE_MEDIA),
            new Transition(CHANGE_APPROVING, EXECUTING, TRIGGER_CHANGE_APPROVED, ROLE_BOSS),
            new Transition(CHANGE_APPROVING, EXECUTING, TRIGGER_CHANGE_REJECTED, ROLE_BOSS),
            new Transition(EXECUTING, EXECUTION_COMPLETED, TRIGGER_COMPLETE_EXECUTION, ROLE_MEDIA),
            new Transition(EXECUTION_COMPLETED, SETTLEMENT, TRIGGER_ENTER_SETTLEMENT, ROLE_SYSTEM),
            new Transition(SETTLEMENT, SETTLEMENT, TRIGGER_SETTLEMENT_UPDATE, ROLE_FINANCE),
            new Transition(SETTLEMENT, ARCHIVED, TRIGGER_ARCHIVE, ROLE_FINANCE),
            new Transition(SETTLEMENT, ARCHIVED, TRIGGER_FORCE_ARCHIVE, ROLE_BOSS),
            new Transition(DRAFT, VOIDED, TRIGGER_VOID, ROLE_MEDIA),
            new Transition(PENDING_BOSS_APPROVAL, VOIDED, TRIGGER_VOID, ROLE_BOSS),
            new Transition(EXECUTING, VOIDED, TRIGGER_VOID, ROLE_BOSS),
            new Transition(CHANGE_APPROVING, VOIDED, TRIGGER_VOID, ROLE_BOSS),
            new Transition(EXECUTION_COMPLETED, VOIDED, TRIGGER_VOID, ROLE_BOSS),
            new Transition(SETTLEMENT, VOIDED, TRIGGER_VOID, ROLE_BOSS)
    );

    /**
     * 审批开关开启时是否允许 from→to。
     */
    public static boolean canTransit(int from, int to) {
        return canTransit(from, to, true);
    }

    /**
     * 是否允许 from→to。
     *
     * @param approvalEnabled 审批开关是否开启（L-14）。关闭时提交(0)直达审核通过(20)。
     */
    public static boolean canTransit(int from, int to, boolean approvalEnabled) {
        if (from == to) {
            return false;
        }
        if (!approvalEnabled && from == DRAFT && to == APPROVED) {
            // L-14 审批开关关闭：草稿提交直达审核通过
            return true;
        }
        return TRANSITIONS.stream().anyMatch(t -> t.from() == from && t.to() == to);
    }

    /**
     * 返回 from 状态下所有可达的下一状态。
     */
    public static List<Integer> nextStates(int from) {
        List<Integer> result = new ArrayList<>();
        for (Transition t : TRANSITIONS) {
            if (t.from() == from) {
                result.add(t.to());
            }
        }
        return result;
    }

    /**
     * 返回 from→to 转换所需的角色；若不存在该转换则返回 null。
     */
    public static String requiredRoleFor(int from, int to) {
        return TRANSITIONS.stream()
                .filter(t -> t.from() == from && t.to() == to)
                .map(Transition::requiredRole)
                .findFirst()
                .orElse(null);
    }

    /**
     * 返回 from→to 转换的触发动作；若不存在该转换则返回 null。
     */
    public static String triggerFor(int from, int to) {
        return TRANSITIONS.stream()
                .filter(t -> t.from() == from && t.to() == to)
                .map(Transition::trigger)
                .findFirst()
                .orElse(null);
    }
}
