package com.sgl.hms.cmn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sgl.hms.model.cmn.Dict;
import com.sgl.hms.model.hosp.HospitalSet;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DictService extends IService<Dict> {
    //根据数据id查询子数据列表
    List<Dict> findChildData(Long id);

    //导出数据字典
    void exportDict(HttpServletResponse response);

    //导入数据字典
    void importDict(MultipartFile file);

    //根据dictCode和value查询
    String getDictName(String dictCode, String value);

    //根据dictCode获取下级节点
    List<Dict> findByDictCode(String dictCode);

}
