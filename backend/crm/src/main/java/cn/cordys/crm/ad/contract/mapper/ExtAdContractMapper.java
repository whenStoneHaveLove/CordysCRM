package cn.cordys.crm.ad.contract.mapper;

import cn.cordys.common.mapper.CommonMapper;
import cn.cordys.crm.ad.contract.domain.AdContract;
import cn.cordys.crm.ad.contract.dto.request.AdContractPageRequest;
import cn.cordys.crm.ad.contract.dto.response.AdContractListResponse;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 合同扩展 Mapper（M5 T-40，列表/复杂查询专用；基础 CRUD 由 {@link BaseMapper} 提供）。
 * XML 与本接口同目录（src/main/java/.../contract/mapper/ExtAdContractMapper.xml）。
 *
 * <p>约定：request 参数统一以 {@code @Param("request")} 暴露，并在 XML 中复用
 * {@code CommonMapper.sort} 动态排序片段；列表查询按 organization_id 做租户隔离、按 entityIds 做主体隔离。</p>
 */
@Mapper
public interface ExtAdContractMapper extends BaseMapper<AdContract> {

    /**
     * 分页列表（多筛选 + 关键字 + 主体隔离 + 排序）。
     *
     * @param request 分页与筛选条件（含 organizationId 与 entityIds 主体隔离集合）
     * @return 列表项
     */
    List<AdContractListResponse> pageList(@Param("request") AdContractPageRequest request);

    /**
     * 统计某组织当天的合同数（用于合同号 CN{yyyyMMdd}-{3位流水} 流水号）。
     */
    long countTodayContracts(@Param("orgId") String orgId,
                              @Param("start") Long start,
                              @Param("end") Long end);

    /**
     * 按订单id查询单笔合同（ad_contract.order_id 直接关联）。
     */
    @org.apache.ibatis.annotations.Select("SELECT * FROM ad_contract WHERE order_id = #{orderId} AND deleted = 0")
    List<AdContract> selectByOrderId(@Param("orderId") String orderId);
}
