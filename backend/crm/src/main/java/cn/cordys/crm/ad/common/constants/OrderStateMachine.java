package cn.cordys.crm.ad.common.constants;

import cn.cordys.common.constants.PermissionConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单主状态机（V3.1 §6.1/§6.2，配置化方式 A，二次开发简化版）。
 *
 * <p>集中维护 {@code (from, to, trigger, requiredRole)} 映射，业务代码统一调用
 * {@link #canTransit(int, int)} / {@link #nextStates(int)} / {@link #requiredRoleFor(int, int)}，
 * <b>禁止</b>在业务逻辑中硬编码状态跳变。</p>
 *
 * <p>状态码采用 10 倍数间隔编码（0/10/.../100），预留后续插入新状态。</p>
 *
 * <p>简化后的主状态（8 个）：
 * <ul>
 *   <li>草稿(0) → 审批中(10) → 待执行(45) → 执行中(50) → 结算中(80) → 已归档(90)</li>
 *   <li>改单：执行中(50) → 改单审核中(60) → 执行中(50)</li>
 *   <li>作废：除已归档(90)外均可作废(100)</li>
 * </ul>
 * </p>
 *
 * <p>关键规则：
 * <ul>
 *   <li>L-14 审批开关：开关关闭时提交(0)直达待执行(45)。</li>
 *   <li>审批通过后不再有「财务前置」推导，直接进入待执行(45)。</li>
 *   <li>执行中(50)到期后由定时任务流转到结算中(80)；结算中满足归档条件后自动归档(90)。</li>
 * </ul>
 * </p>
 */
@Slf4j
public final class OrderStateMachine {

    private OrderStateMachine() {
    }

    /* ---------- 主状态码（10 倍数编码） ---------- */
    public static final int DRAFT = 0;                    // 草稿
    public static final int PENDING_BOSS_APPROVAL = 10;  // 审批中
    public static final int PENDING_EXECUTE = 45;        // 待执行
    public static final int EXECUTING = 50;              // 执行中
    public static final int CHANGE_APPROVING = 60;       // 改单审核中
    public static final int SETTLEMENT = 80;             // 结算中
    public static final int ARCHIVED = 90;               // 已归档
    public static final int VOIDED = 100;                // 已作废

    /* ---------- 角色（requiredRole，已废弃，保留仅作兼容占位，不再用于权限校验） ---------- */
    @Deprecated
    public static final String ROLE_MEDIA = "ROLE_MEDIA";
    @Deprecated
    public static final String ROLE_FINANCE = "ROLE_FINANCE";
    @Deprecated
    public static final String ROLE_BOSS = "ROLE_BOSS";
    /** 系统级流转（定时任务自动执行）标识，无对应权限码。 */
    public static final String ROLE_SYSTEM = "SYSTEM";

    /* ---------- 触发动作（trigger） ---------- */
    public static final String TRIGGER_SUBMIT = "SUBMIT";
    public static final String TRIGGER_REJECT = "REJECT";
    public static final String TRIGGER_APPROVE = "APPROVE";
    public static final String TRIGGER_CONFIRM_EXECUTE = "CONFIRM_EXECUTE";
    public static final String TRIGGER_APPLY_CHANGE = "APPLY_CHANGE";
    public static final String TRIGGER_CHANGE_APPROVED = "CHANGE_APPROVED";
    public static final String TRIGGER_CHANGE_REJECTED = "CHANGE_REJECTED";
    public static final String TRIGGER_AUTO_OVERDUE = "AUTO_OVERDUE";
    public static final String TRIGGER_AUTO_ARCHIVE = "AUTO_ARCHIVE";
    public static final String TRIGGER_FORCE_ARCHIVE = "FORCE_ARCHIVE";
    public static final String TRIGGER_VOID = "VOID";

    /** 状态转换定义。 */
    private record Transition(int from, int to, String trigger, String requiredRole) {
    }

    private static final List<Transition> TRANSITIONS = List.of(
            // 提交 / 审批（权限码见 PermissionConstants）
            new Transition(DRAFT, PENDING_BOSS_APPROVAL, TRIGGER_SUBMIT, PermissionConstants.AD_ORDER_SUBMIT),
            new Transition(PENDING_BOSS_APPROVAL, DRAFT, TRIGGER_REJECT, PermissionConstants.AD_ORDER_REJECT),
            new Transition(PENDING_BOSS_APPROVAL, PENDING_EXECUTE, TRIGGER_APPROVE, PermissionConstants.AD_ORDER_APPROVE),
            // 确认执行
            new Transition(PENDING_EXECUTE, EXECUTING, TRIGGER_CONFIRM_EXECUTE, PermissionConstants.AD_ORDER_CONFIRM_EXECUTE),
            // 改单（申请/通过/驳回，权限码归属 AD_ORDER_CHANGE）
            new Transition(EXECUTING, CHANGE_APPROVING, TRIGGER_APPLY_CHANGE, PermissionConstants.AD_ORDER_CHANGE_SUBMIT),
            new Transition(CHANGE_APPROVING, EXECUTING, TRIGGER_CHANGE_APPROVED, PermissionConstants.AD_ORDER_CHANGE_APPROVE),
            new Transition(CHANGE_APPROVING, EXECUTING, TRIGGER_CHANGE_REJECTED, PermissionConstants.AD_ORDER_CHANGE_REJECT),
            // 到期自动结算 / 自动归档（系统级）
            new Transition(EXECUTING, SETTLEMENT, TRIGGER_AUTO_OVERDUE, ROLE_SYSTEM),
            new Transition(SETTLEMENT, ARCHIVED, TRIGGER_AUTO_ARCHIVE, ROLE_SYSTEM),
            new Transition(SETTLEMENT, ARCHIVED, TRIGGER_FORCE_ARCHIVE, PermissionConstants.AD_ORDER_FORCE_ARCHIVE),
            // 作废（除已归档外均可）
            new Transition(DRAFT, VOIDED, TRIGGER_VOID, PermissionConstants.AD_ORDER_VOID),
            new Transition(PENDING_BOSS_APPROVAL, VOIDED, TRIGGER_VOID, PermissionConstants.AD_ORDER_VOID),
            new Transition(PENDING_EXECUTE, VOIDED, TRIGGER_VOID, PermissionConstants.AD_ORDER_VOID),
            new Transition(EXECUTING, VOIDED, TRIGGER_VOID, PermissionConstants.AD_ORDER_VOID),
            new Transition(CHANGE_APPROVING, VOIDED, TRIGGER_VOID, PermissionConstants.AD_ORDER_VOID),
            new Transition(SETTLEMENT, VOIDED, TRIGGER_VOID, PermissionConstants.AD_ORDER_VOID)
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
     * @param approvalEnabled 审批开关是否开启（L-14）。关闭时提交(0)直达待执行(45)。
     */
    public static boolean canTransit(int from, int to, boolean approvalEnabled) {
        if (from == to) {
            return false;
        }
        if (!approvalEnabled && from == DRAFT && to == PENDING_EXECUTE) {
            // L-14 审批开关关闭：草稿提交直达待执行
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
     * 返回 from→to 转换所需的权限码（见 PermissionConstants）。
     * 系统级流转返回 {@link #ROLE_SYSTEM}（"SYSTEM"，表示无需权限码校验）。
     * 若不存在该转换则返回 null。
     */
    public static String requiredRoleFor(int from, int to) {
        return TRANSITIONS.stream()
                .filter(t -> t.from() == from && t.to() == to)
                .map(Transition::requiredRole)
                .findFirst()
                .orElse(null);
    }

    /**
     * 返回 from→to 转换所需的权限码（语义同 {@link #requiredRoleFor}，方法别名）。
     */
    public static String requiredPermissionFor(int from, int to) {
        return requiredRoleFor(from, to);
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
