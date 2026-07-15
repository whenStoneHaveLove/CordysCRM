package cn.cordys.crm.ad.common.mapper;

import cn.cordys.crm.ad.common.domain.AdOperationLog;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 跨模块操作日志 Mapper（由 {@code @OperationLog} 切面写入，V3.1 §5.2 L-18）。
 */
@Mapper
public interface ExtAdOperationLogMapper extends BaseMapper<AdOperationLog> {
}
