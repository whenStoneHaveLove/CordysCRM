package cn.cordys.crm.ad.support.mapper;

import cn.cordys.common.mapper.CommonMapper;
import cn.cordys.crm.ad.support.domain.AdSupportTicket;
import cn.cordys.crm.ad.support.dto.request.AdSupportTicketPageRequest;
import cn.cordys.crm.ad.support.dto.response.AdSupportTicketListResponse;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 广告支持工单扩展 Mapper（M6，列表/复杂查询专用；基础 CRUD 由 {@link BaseMapper} 提供）。
 * XML 与本接口同目录（src/main/java/.../support/mapper/ExtAdSupportTicketMapper.xml）。
 */
@Mapper
public interface ExtAdSupportTicketMapper extends BaseMapper<AdSupportTicket> {

    /**
     * 分页列表（多筛选 + 关键字 + 排序）。
     */
    List<AdSupportTicketListResponse> pageList(@Param("request") AdSupportTicketPageRequest request);
}
