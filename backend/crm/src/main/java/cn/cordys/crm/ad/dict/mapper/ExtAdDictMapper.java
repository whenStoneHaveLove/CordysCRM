package cn.cordys.crm.ad.dict.mapper;

import cn.cordys.crm.ad.dict.domain.AdDict;
import cn.cordys.crm.ad.dict.dto.request.AdDictPageRequest;
import cn.cordys.crm.ad.dict.dto.response.AdDictListResponse;
import cn.cordys.mybatis.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 字典 Mapper（V3.1 §5.3/§4.3）。
 */
@Mapper
public interface ExtAdDictMapper extends BaseMapper<AdDict> {

    @Select("SELECT * FROM ad_dict WHERE dict_code = #{dictCode} AND status = 10 AND deleted = 0 ORDER BY sort ASC")
    List<AdDict> selectByDictCode(@Param("dictCode") String dictCode);

    /**
     * 字典项分页（V3.1 §4.3，B-4）。SQL 见同目录 ExtAdDictMapper.xml。
     */
    List<AdDictListResponse> pageList(AdDictPageRequest request);
}
