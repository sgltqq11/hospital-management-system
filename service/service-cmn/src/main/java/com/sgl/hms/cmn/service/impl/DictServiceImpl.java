package com.sgl.hms.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgl.hms.cmn.listener.ImportDictListener;
import com.sgl.hms.cmn.mapper.DictMapper;
import com.sgl.hms.cmn.service.DictService;
import com.sgl.hms.model.cmn.Dict;
import com.sgl.hms.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
    //根据数据id查询子数据列表
    @Cacheable(value = "dict",keyGenerator = "keyGenerator")
    @Override
    public List<Dict> findChildData(Long id) {
        //查询该 Long id（id=10000） 下的子数据
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        //select * from dict where parent_id = 10000;
        queryWrapper.eq("parent_id",id);
        List<Dict> dictList = baseMapper.selectList(queryWrapper);

        //使用CollectionUtils.isEmpty()方法判空
        if (!CollectionUtils.isEmpty(dictList)){
            //向list集合每个dict对象中设置hasChildren
            for (Dict dict : dictList) {
                Long dictId = dict.getId();
                //Long dictId = 10001/10002/10003/10004/100005
                boolean isChild = this.isChildData(dictId);
                dict.setHasChildren(isChild);
            }
        }
        return dictList;
    }
    //判断id下是否有子节点
    public boolean isChildData(Long id){
        //Long id = 10001/10002/10003/10004/100005
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id",id);
        Integer count = baseMapper.selectCount(queryWrapper);
        return count > 0;
    }

    //导出数据字典
    @Override
    public void exportDict(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("dict", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");
            //查询数据
            List<Dict> dictList = baseMapper.selectList(null);
            //Dict --> DictEeVo
            List<DictEeVo> dictVoList = new ArrayList<>(dictList.size());
            for(Dict dict : dictList) {
                DictEeVo dictEeVo = new DictEeVo();
                BeanUtils.copyProperties(dict,dictEeVo,DictEeVo.class);
                dictVoList.add(dictEeVo);
            }
            try {
                EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("数据字典").doWrite(dictVoList);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //导入数据字典 allEntries = true: 方法调用后清空所有缓存
    @CacheEvict(value = "dict", allEntries=true)
    @Override
    public void importDict(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(),DictEeVo.class,new ImportDictListener(baseMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //根据dictCode和value查询
    @Override
    public String getDictName(String dictCode, String value) {
        //如果dictCode为空,直接根据value进行查询
        if (StringUtils.isEmpty(dictCode)){
            //直接根据value进行查询
            QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("value",value);
            Dict dict = baseMapper.selectOne(queryWrapper);
            if(null != dict) {
                return dict.getName();
            }
        }else {
            //根据dictCode查询dict对象，得到dict的id值
            QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("dict_code",dictCode);
            Dict dict = baseMapper.selectOne(queryWrapper);
            if(null == dict) return "";
            //根据parent_id和value进行查询
            Long parentId = dict.getId(); //查询id对应下parent_id等于id的dict，然后返回名称
            Dict dict2 = baseMapper.selectOne(new QueryWrapper<Dict>()
                    .eq("parent_id", parentId)
                    .eq("value", value));
            if(null != dict2) {
                return dict2.getName();
            }
        }
        return "";
    }

    //根据dictCode获取下级节点
    @Override
    public List<Dict> findByDictCode(String dictCode) {

        //根据dictCode查询对应的dict的id
        Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("dict_code", dictCode));
        if (null != dict){
            return this.findChildData(dict.getId());
        }
        return null;
    }


}
