package cn.cordys.crm.ad.dict.service;

import cn.cordys.crm.ad.dict.domain.AdDict;
import cn.cordys.crm.ad.dict.mapper.ExtAdDictMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典服务（V3.1 §5.3/§4.3）。按 dict_code 查询启用的字典项。
 */
@Service
public class AdDictService {

    @Resource
    private ExtAdDictMapper dictMapper;

    public String save(AdDict dict) {
        dictMapper.insert(dict);
        return dict.getId();
    }

    public void update(AdDict dict) {
        dictMapper.updateById(dict);
    }

    public void remove(String id) {
        dictMapper.deleteByPrimaryKey(id);
    }

    public AdDict get(String id) {
        return dictMapper.selectByPrimaryKey(id);
    }

    /**
     * 按字典编码查询启用的字典项（用于下拉选项）。
     */
    public List<AdDict> listByDictCode(String dictCode) {
        return dictMapper.selectByDictCode(dictCode);
    }
}
