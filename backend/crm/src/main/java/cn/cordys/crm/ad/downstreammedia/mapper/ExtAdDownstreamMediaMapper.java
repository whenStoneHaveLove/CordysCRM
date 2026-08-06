package cn.cordys.crm.ad.downstreammedia.mapper;

import cn.cordys.crm.ad.downstreammedia.domain.AdDownstreamMedia;
import cn.cordys.crm.ad.downstreammedia.dto.request.AdDownstreamMediaPageRequest;
import cn.cordys.crm.ad.downstreammedia.dto.response.AdDownstreamMediaListResponse;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExtAdDownstreamMediaMapper extends BaseMapper<AdDownstreamMedia> {

    List<AdDownstreamMediaListResponse> pageList(@Param("request") AdDownstreamMediaPageRequest request);
}
