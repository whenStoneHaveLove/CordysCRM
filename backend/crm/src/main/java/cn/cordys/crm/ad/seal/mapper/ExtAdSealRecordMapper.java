package cn.cordys.crm.ad.seal.mapper;

import cn.cordys.common.mapper.CommonMapper;
import cn.cordys.crm.ad.contract.domain.AdSealRecord;
import cn.cordys.crm.ad.seal.dto.request.AdSealRecordPageRequest;
import cn.cordys.crm.ad.seal.dto.response.AdSealRecordListResponse;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用印记录扩展 Mapper（M5 T-42，列表/复杂查询专用；基础 CRUD 由 {@link BaseMapper} 提供）。
 * XML 与本接口同目录（src/main/java/.../seal/mapper/ExtAdSealRecordMapper.xml）。
 *
 * <p>列表查询按 organization_id 做租户隔离、按 entityIds 做主体隔离，并复用 {@code CommonMapper.sort} 动态排序。</p>
 */
@Mapper
public interface ExtAdSealRecordMapper extends BaseMapper<AdSealRecord> {

    /**
     * 分页列表（多筛选 + 关键字 + 主体隔离 + 排序）。
     *
     * @param request 分页与筛选条件（含 organizationId 与 entityIds 主体隔离集合）
     * @return 列表项
     */
    List<AdSealRecordListResponse> pageList(@Param("request") AdSealRecordPageRequest request);
}
