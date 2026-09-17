package cn.cordys.common.constants;

import lombok.Getter;

import java.util.Arrays;

/**
 * 系统内置角色ID
 *
 * @author jianxing
 */
@Getter
public enum InternalRole {
    ORG_ADMIN("org_admin", false),
    SALES_MANAGER("sales_manager", true),
    SALES_STAFF("sales_staff", true);

    private final String value;

    /**
     * 是否从所有角色列表中隐藏（软删除效果）。
     *
     * <p>为 true 的角色不出现在任何角色列表 / 下拉 / 角色树里，
     * 其行数据与权限配置仍保留在库中；需要恢复时把这里改回 false 即可。</p>
     */
    private final boolean hidden;

    InternalRole(String value, boolean hidden) {
        this.value = value;
        this.hidden = hidden;
    }

    /**
     * 该角色id 是否为「已隐藏的内置角色」
     *
     * @param roleId 角色id
     *
     * @return 非内置角色一律返回 false
     */
    public static boolean isHidden(String roleId) {
        return Arrays.stream(values()).anyMatch(role -> role.hidden && role.value.equals(roleId));
    }
}
