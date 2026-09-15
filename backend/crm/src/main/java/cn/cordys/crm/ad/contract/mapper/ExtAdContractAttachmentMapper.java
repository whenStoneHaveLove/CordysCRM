package cn.cordys.crm.ad.contract.mapper;

import cn.cordys.crm.ad.contract.domain.AdContractAttachment;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 合同附件 Mapper（自定义查询走 BaseMapper，列表按合同 id 查询）。
 */
@Mapper
public interface ExtAdContractAttachmentMapper extends BaseMapper<AdContractAttachment> {

    @Select("SELECT * FROM ad_contract_attachment WHERE contract_id = #{contractId} AND deleted = 0")
    List<AdContractAttachment> selectByContractId(@Param("contractId") String contractId);

    @Select("SELECT * FROM ad_contract_attachment WHERE contract_id = #{contractId} AND type = #{type} AND deleted = 0")
    List<AdContractAttachment> selectByContractIdAndType(@Param("contractId") String contractId, @Param("type") int type);
}
