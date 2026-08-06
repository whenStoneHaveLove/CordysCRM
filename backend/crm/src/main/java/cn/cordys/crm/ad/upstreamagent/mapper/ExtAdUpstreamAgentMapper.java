package cn.cordys.crm.ad.upstreamagent.mapper;

import cn.cordys.crm.ad.upstreamagent.domain.AdUpstreamAgent;
import cn.cordys.crm.ad.upstreamagent.dto.request.AdUpstreamAgentPageRequest;
import cn.cordys.crm.ad.upstreamagent.dto.response.AdUpstreamAgentListResponse;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExtAdUpstreamAgentMapper extends BaseMapper<AdUpstreamAgent> {

    List<AdUpstreamAgentListResponse> pageList(@Param("request") AdUpstreamAgentPageRequest request);
}
