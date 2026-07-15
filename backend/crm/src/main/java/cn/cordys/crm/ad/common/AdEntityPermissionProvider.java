package cn.cordys.crm.ad.common;

import cn.cordys.security.SessionUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 业务主体数据隔离提供者（V3.1 §3.4，T-03）。
 *
 * <p>替换开源 {@code DataScopeService}：列表/统计查询按主体隔离，
 * {@code is_cross_entity = 1} → 返回 null（调用方不加过滤，即全量）；
 * 否则返回当前用户关联的业务主体集合（调用方拼 {@code business_entity_id IN (...)}）。</p>
 *
 * <p>供后续 Ext*Mapper.list 复用（M2 接入）：在 SQL 中依据本方法返回的集合构造过滤条件。</p>
 */
@Component
public class AdEntityPermissionProvider {

    @Resource
    private AdUserEntityContext userEntityContext;

    /**
     * 解析当前用户可见的业务主体集合。
     *
     * @return 主体 id 列表；跨主体用户返回 {@code null}（表示全量，不加过滤）
     */
    public List<String> resolveVisibleEntityIds() {
        String userId = SessionUtils.getUserId();
        if (userId == null) {
            return null;
        }
        if (userEntityContext.isCrossEntity(userId)) {
            return null;
        }
        return userEntityContext.getEntityIds(userId);
    }

    /**
     * 供 Ext*Mapper.list 复用的过滤条件构造（M2 接入）。
     *
     * @return 需 {@code IN} 过滤的主体 id 集合；跨主体返回 {@code null}
     */
    public List<String> buildEntityFilter() {
        return resolveVisibleEntityIds();
    }

    /**
     * 当前用户是否为跨主体可见（全量）。
     */
    public boolean isCrossEntity() {
        String userId = SessionUtils.getUserId();
        return userId != null && userEntityContext.isCrossEntity(userId);
    }
}
