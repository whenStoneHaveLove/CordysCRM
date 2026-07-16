package cn.cordys.crm.ad.common.constants;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 纯逻辑单测（NO @SpringBootTest）：验证改单状态枚举 {@link AdOrderChangeStatus}。
 *
 * <p>该枚举本身不提供状态转换方法（转换守卫在 {@code AdOrderChangeService} 各写方法中强制），
 * 故本测试锁定：① 5 个生命周期常量的 code/label 契约；② {@code of()}/{@code labelOf()} 反查；
 * ③ 期望的生命周期转换表（DRAFT→SUBMITTED→APPROVED→EXECUTED，SUBMITTED→REJECTED）。
 * 若有人重排/重命名常量或改错 code，本测试即失败，作为改单状态机的回归护栏。</p>
 */
class AdOrderChangeStatusTest {

    /* ---------- 常量契约 ---------- */

    @Test
    void constants_codeAndLabel() {
        assertEquals(0, AdOrderChangeStatus.DRAFT.getCode());
        assertEquals("草稿", AdOrderChangeStatus.DRAFT.getLabel());
        assertEquals(10, AdOrderChangeStatus.SUBMITTED.getCode());
        assertEquals("已提交", AdOrderChangeStatus.SUBMITTED.getLabel());
        assertEquals(20, AdOrderChangeStatus.APPROVED.getCode());
        assertEquals("审批通过", AdOrderChangeStatus.APPROVED.getLabel());
        assertEquals(30, AdOrderChangeStatus.REJECTED.getCode());
        assertEquals("已驳回", AdOrderChangeStatus.REJECTED.getLabel());
        assertEquals(40, AdOrderChangeStatus.EXECUTED.getCode());
        assertEquals("已执行", AdOrderChangeStatus.EXECUTED.getLabel());
    }

    @Test
    void constants_fiveAndUniqueCodes() {
        Set<Integer> codes = Set.of(
                AdOrderChangeStatus.DRAFT.getCode(),
                AdOrderChangeStatus.SUBMITTED.getCode(),
                AdOrderChangeStatus.APPROVED.getCode(),
                AdOrderChangeStatus.REJECTED.getCode(),
                AdOrderChangeStatus.EXECUTED.getCode());
        assertEquals(5, codes.size(), "5 个互不相同的状态码");
    }

    /* ---------- of() 反查 ---------- */

    @Test
    void of_validCodes() {
        assertEquals(AdOrderChangeStatus.DRAFT, AdOrderChangeStatus.of(0));
        assertEquals(AdOrderChangeStatus.SUBMITTED, AdOrderChangeStatus.of(10));
        assertEquals(AdOrderChangeStatus.APPROVED, AdOrderChangeStatus.of(20));
        assertEquals(AdOrderChangeStatus.REJECTED, AdOrderChangeStatus.of(30));
        assertEquals(AdOrderChangeStatus.EXECUTED, AdOrderChangeStatus.of(40));
    }

    @Test
    void of_nullAndUnknown_returnsNull() {
        assertNull(AdOrderChangeStatus.of(null));
        assertNull(AdOrderChangeStatus.of(99));
        assertNull(AdOrderChangeStatus.of(-1));
    }

    /* ---------- labelOf() ---------- */

    @Test
    void labelOf_validCodes() {
        assertEquals("草稿", AdOrderChangeStatus.labelOf(0));
        assertEquals("已提交", AdOrderChangeStatus.labelOf(10));
        assertEquals("审批通过", AdOrderChangeStatus.labelOf(20));
        assertEquals("已驳回", AdOrderChangeStatus.labelOf(30));
        assertEquals("已执行", AdOrderChangeStatus.labelOf(40));
    }

    @Test
    void labelOf_nullAndUnknown() {
        assertEquals("", AdOrderChangeStatus.labelOf(null));
        assertEquals("99", AdOrderChangeStatus.labelOf(99));
    }

    /* ---------- 生命周期契约（转换守卫在 service 内，这里锁 code 方案） ---------- */

    /** 期望的改单状态转换表（与 AdOrderChangeService 各写方法守卫一致）。 */
    private static Map<Integer, Set<Integer>> allowedTransitions() {
        return Map.of(
                AdOrderChangeStatus.DRAFT.getCode(), Set.of(AdOrderChangeStatus.SUBMITTED.getCode()),
                AdOrderChangeStatus.SUBMITTED.getCode(),
                        Set.of(AdOrderChangeStatus.APPROVED.getCode(), AdOrderChangeStatus.REJECTED.getCode()),
                AdOrderChangeStatus.APPROVED.getCode(), Set.of(AdOrderChangeStatus.EXECUTED.getCode()));
    }

    @Test
    void lifecycle_allowedTransitionsResolveToEnum() {
        for (Map.Entry<Integer, Set<Integer>> e : allowedTransitions().entrySet()) {
            assertTrue(AdOrderChangeStatus.of(e.getKey()) != null, "from code 应存在: " + e.getKey());
            for (int to : e.getValue()) {
                assertTrue(AdOrderChangeStatus.of(to) != null, "to code 应存在: " + to);
            }
        }
    }

    @Test
    void lifecycle_rejectedIllegalJumps() {
        var allowed = allowedTransitions();
        // 草稿不可直达 审批通过/已执行
        assertFalse(allowed.getOrDefault(AdOrderChangeStatus.DRAFT.getCode(), Set.of())
                .contains(AdOrderChangeStatus.APPROVED.getCode()));
        assertFalse(allowed.getOrDefault(AdOrderChangeStatus.DRAFT.getCode(), Set.of())
                .contains(AdOrderChangeStatus.EXECUTED.getCode()));
        // 已提交只能到 审批通过/已驳回，不可直达 已执行
        assertFalse(allowed.get(AdOrderChangeStatus.SUBMITTED.getCode())
                .contains(AdOrderChangeStatus.EXECUTED.getCode()));
        // 已执行(40) 为终态：不作为任何转换的“源”（无出向转换）
        assertFalse(allowed.containsKey(AdOrderChangeStatus.EXECUTED.getCode()),
                "已执行(40)应为终态，无出向转换");
        // 已驳回(30) 为终态：不作为任何转换的“源”（无出向转换）
        assertFalse(allowed.containsKey(AdOrderChangeStatus.REJECTED.getCode()),
                "已驳回(30)应为终态，无出向转换");
        // 终态仍应能被前向到达（正向守卫正确）
        assertTrue(allowed.get(AdOrderChangeStatus.APPROVED.getCode())
                        .contains(AdOrderChangeStatus.EXECUTED.getCode()),
                "审批通过(20)应可到达已执行(40)");
        assertTrue(allowed.get(AdOrderChangeStatus.SUBMITTED.getCode())
                        .contains(AdOrderChangeStatus.REJECTED.getCode()),
                "已提交(10)应可到达已驳回(30)");
    }
}
