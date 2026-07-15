package cn.cordys.crm.ad.common;

import cn.cordys.crm.ad.businessentity.service.AdUserBusinessEntityService;
import cn.cordys.crm.ad.common.mapper.SysUserCrossEntityMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 当前用户业务主体上下文（V3.1 §3.4，L-06/L-12）。
 *
 * <p>封装两类判断：
 * <ul>
 *   <li>是否跨主体可见（sys_user.is_cross_entity = 1 → 全量）；</li>
 *   <li>可见的业务主体集合（ad_user_business_entity 中该用户的主体）。</li>
 * </ul>
 * 供 {@link AdEntityPermissionProvider} 在列表/统计查询中复用。</p>
 */
@Component
public class AdUserEntityContext {

    @Resource
    private AdUserBusinessEntityService userBusinessEntityService;

    @Resource
    private SysUserCrossEntityMapper sysUserCrossEntityMapper;

    /**
     * 用户是否为跨主体可见（财务/老板）。
     */
    public boolean isCrossEntity(String userId) {
        if (userId == null) {
            return false;
        }
        Integer value = sysUserCrossEntityMapper.selectIsCrossEntity(userId);
        return value != null && value == 1;
    }

    /**
     * 用户可见的业务主体 id 集合（不含已删除）。
     */
    public List<String> getEntityIds(String userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return userBusinessEntityService.selectBusinessEntityIds(userId);
    }
}
