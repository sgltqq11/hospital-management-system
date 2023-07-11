package com.sgl.hms.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.sgl.hms.cmn.mapper.DictMapper;
import com.sgl.hms.model.cmn.Dict;
import com.sgl.hms.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;

public class ImportDictListener extends AnalysisEventListener<DictEeVo> {

    private DictMapper dictMapper;

    //通过构造方法注入mapper
    public ImportDictListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    //一行一行的读取
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        //调用方法添加数据库
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo,dict,Dict.class);
        //插入数据库
        dictMapper.insert(dict);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
