package com.sgl.hms.oss.controller;

import com.sgl.hms.common.result.Result;
import com.sgl.hms.oss.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@RequestMapping("/api/oss/file")
@Api(tags = "文件上传")
public class FileApiController {

    @Autowired
    private FileService fileService;

    //上传文件到阿里云oss
    @ApiOperation(value = "上传文件到阿里云oss")
    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file) {
        //获取上传文件
        String url = fileService.upload(file);   //url就是图片的路径
        return Result.ok(url);
    }
}
