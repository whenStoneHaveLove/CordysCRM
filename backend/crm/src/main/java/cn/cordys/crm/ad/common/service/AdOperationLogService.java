package cn.cordys.crm.ad.common.service;

import cn.cordys.common.uid.IDGenerator;
import cn.cordys.crm.ad.common.domain.AdOperationLog;
import cn.cordys.crm.ad.common.mapper.ExtAdOperationLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 跨模块操作日志服务（V3.1 §5.2 L-18）。由 {@code @OperationLog} 切面调用写入。
 */
@Service
public class AdOperationLogService {

    @Resource
    private ExtAdOperationLogMapper operationLogMapper;

    public void save(AdOperationLog log) {
        if (log.getId() == null || log.getId().isEmpty()) {
            log.setId(IDGenerator.nextStr());
        }
        if (log.getCreateTime() == null) {
            log.setCreateTime(System.currentTimeMillis());
        }
        operationLogMapper.insert(log);
    }
}
