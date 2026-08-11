package cn.cordys.crm.ad.common.service;

import cn.cordys.crm.ad.common.dto.request.AdOperationLogPageRequest;
import cn.cordys.crm.ad.common.dto.response.AdOperationLogResponse;
import cn.cordys.crm.ad.common.mapper.ExtAdOperationLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 广告操作日志查询服务。
 */
@Service
public class AdOperationLogQueryService {

    @Resource
    private ExtAdOperationLogMapper operationLogMapper;

    public List<AdOperationLogResponse> list(AdOperationLogPageRequest request, String orgId) {
        return operationLogMapper.list(request, orgId);
    }
}
