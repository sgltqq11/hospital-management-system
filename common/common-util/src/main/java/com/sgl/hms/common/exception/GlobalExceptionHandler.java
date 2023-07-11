package com.sgl.hms.common.exception;

import com.sgl.hms.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理类
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result err(Exception e){
        e.printStackTrace();
        return Result.fail();
    }

    /**
     * 自定义异常处理方法
     * @param e
     * @return
     */
    @ExceptionHandler(CustomHmsGlobalException.class)
    @ResponseBody
    public Result err(CustomHmsGlobalException e){
        e.printStackTrace();
        return Result.build(e.getCode(),e.getMessage());
    }
}
