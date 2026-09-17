package cn.cordys.common.constants;

import java.util.Set;

/**
 * 「仅系统管理员（admin 用户）可用」的权限清单。
 *
 * <p><b>作用范围</b>：这里列出的权限在 {@code PermissionCache#getPermissionIds} 出口处被过滤，
 * 非 admin 用户永远拿不到这些权限标识，因此：</p>
 * <ul>
 *     <li>后端接口校验（{@code PermissionUtils#hasPermission}）不通过，直接 403；</li>
 *     <li>前端菜单、路由、按钮都按权限标识渲染，会自动隐藏，无需改前端；</li>
 *     <li>admin 用户在 {@code PermissionUtils} / 前端 {@code hasPermission} 中都是直接放行，不受影响。</li>
 * </ul>
 *
 * <p><b>数据库不受影响</b>：sys_role_permission 里的授权数据原样保留，
 * 需要恢复时把 {@link #ADMIN_ONLY_PREFIXES} 清空即可（或注释掉调用处）。</p>
 *
 * @author cordys
 */
public class AdminOnlyPermission {

    /**
     * 仅管理员可用的权限前缀：
     * <ul>
     *     <li>MODULE_SETTING  模块配置</li>
     *     <li>SYSTEM_NOTICE   消息设置</li>
     *     <li>PROCESS_SETTING 流程设置</li>
     *     <li>SYSTEM_SETTING  企业设置</li>
     *     <li>OPERATION_LOG   系统日志</li>
     * </ul>
     */
    private static final Set<String> ADMIN_ONLY_PREFIXES = Set.of(
            "MODULE_SETTING:",
            "SYSTEM_NOTICE:",
            "PROCESS_SETTING:",
            "SYSTEM_SETTING:",
            "OPERATION_LOG:"
    );

    /**
     * 非管理员在「角色权限」页可见的一级模块（permission.json 里的一级 id）。
     *
     * <p>当前只放开「广告下单系统」。想恢复原样（所有模块都可见）把这里改成 {@code Set.of()} 即可，
     * 改成空集合后本类只保留 {@link #ADMIN_ONLY_PREFIXES} 的过滤效果。</p>
     */
    private static final Set<String> NON_ADMIN_VISIBLE_MODULES = Set.of(
            "ADVERTISING"
    );

    private AdminOnlyPermission() {
    }

    /**
     * 是否为「仅管理员可用」的权限
     *
     * @param permissionId 权限标识，如 {@code MODULE_SETTING:READ}
     *
     * @return true 表示只允许 admin 用户使用
     */
    public static boolean isAdminOnly(String permissionId) {
        return permissionId != null && ADMIN_ONLY_PREFIXES.stream().anyMatch(permissionId::startsWith);
    }

    /**
     * 非管理员能否看到某个一级模块（permission.json 里的第一层 id）
     *
     * <p>白名单为空表示不限制模块，全部可见。</p>
     *
     * @param moduleId 一级模块id，如 {@code ADVERTISING}
     *
     * @return true 表示非管理员可见
     */
    public static boolean isVisibleModuleForNonAdmin(String moduleId) {
        return NON_ADMIN_VISIBLE_MODULES.isEmpty() || NON_ADMIN_VISIBLE_MODULES.contains(moduleId);
    }
}
