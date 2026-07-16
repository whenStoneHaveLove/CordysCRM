package cn.cordys.crm.ad.payment.mapper;

import cn.cordys.common.mapper.CommonMapper;
import cn.cordys.crm.ad.payment.domain.AdPaymentRecord;
import cn.cordys.crm.ad.payment.dto.request.AdPaymentRecordPageRequest;
import cn.cordys.crm.ad.payment.dto.response.AdPaymentRecordListResponse;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收付款记录扩展 Mapper（M4 T-30，列表/复杂查询专用；基础 CRUD 由 {@link BaseMapper} 提供）。
 * XML 与本接口同目录（src/main/java/.../payment/mapper/ExtAdPaymentRecordMapper.xml）。
 *
 * <p>列表查询复用 {@code CommonMapper.sort} 动态排序片段，并通过 {@code request.entityIds}
 * 注入业务主体隔离（{@code AdEntityPermissionProvider}，L-06/L-12）。</p>
 */
@Mapper
public interface ExtAdPaymentRecordMapper extends BaseMapper<AdPaymentRecord> {

    /**
     * 收付款记录分页列表（多筛选 + 关键字 + 主体隔离 + 排序）。
     *
     * @param request 分页与筛选条件（含 organizationId 与 entityIds 主体隔离集合）
     * @return 列表项
     */
    List<AdPaymentRecordListResponse> pageList(@Param("request") AdPaymentRecordPageRequest request);
}
