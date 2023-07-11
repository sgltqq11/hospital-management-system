package com.sgl.hms.cmn.controller;

import com.sgl.hms.cmn.service.DictService;
import com.sgl.hms.common.result.Result;
import com.sgl.hms.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(tags = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
@CrossOrigin //解决跨域问题
public class DictController {

    @Autowired
    private DictService dictService;

    //根据数据id查询子数据列表
    @ApiOperation(value = "根据数据id查询子数据列表")
    @GetMapping("findChildData/{id}")
    public Result findChildData(@Param("数据id") @PathVariable("id") Long id) {
        List<Dict> list = dictService.findChildData(id);
        return Result.ok(list);
    }


    //直接导出数据字典（无需返回值）
    @ApiOperation(value = "导出数据字典")
    @GetMapping("exportDict")
    public void exportDict(HttpServletResponse response){
        dictService.exportDict(response);
    }

    //导入数据字典
    @ApiOperation(value = "导入数据字典")
    @PostMapping("importDict")
    public void importDict(MultipartFile file){
        dictService.importDict(file);
    }

    //根据dictCode和value查询
    @ApiOperation(value = "获取数据字典名称")
    @GetMapping("getName/{dictCode}/{value}")
    public String getName(
            @ApiParam(name = "dictCode", value = "上级编码", required = true)
            @PathVariable("dictCode") String dictCode,
            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value){
        String dictName = dictService.getDictName(dictCode,value);
        return dictName;
    }

    //根据value查询
    @ApiOperation(value = "获取数据字典名称")
    @GetMapping("getName/{value}")
    public String getName(@ApiParam(name = "value", value = "值", required = true)
                          @PathVariable("value") String value){
        String dictName = dictService.getDictName("",value);
        return dictName;
    }

    //根据dictCode获取下级节点
    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping(value = "/findByDictCode/{dictCode}")
    public Result findByDictCode(@ApiParam(name = "dictCode", value = "上级编码", required = true)
                          @PathVariable("dictCode") String dictCode){
        List<Dict> list = dictService.findByDictCode(dictCode);
        return Result.ok(list);
    }


}
