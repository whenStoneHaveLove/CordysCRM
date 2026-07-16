package cn.cordys.crm.ad.customer.mapper;

import cn.cordys.common.mapper.CommonMapper;
import cn.cordys.crm.ad.customer.domain.AdCustomer;
import cn.cordys.crm.ad.customer.dto.request.AdCustomerPageRequest;
import cn.cordys.crm.ad.customer.dto.response.AdCustomerListResponse;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 广告客户扩展 Mapper（M6，列表/复杂查询专用；基础 CRUD 由 {@link BaseMapper} 提供）。
 * XML 与本接口同目录（src/main/java/.../customer/mapper/ExtAdCustomerMapper.xml）。
 */
@Mapper
public interface ExtAdCustomerMapper extends BaseMapper<AdCustomer> {

    /**
     * 分页列表（多筛选 + 关键字 + 主体隔离 + 排序）。
     */
    List<AdCustomerListResponse> pageList(@Param("request") AdCustomerPageRequest request);
}
