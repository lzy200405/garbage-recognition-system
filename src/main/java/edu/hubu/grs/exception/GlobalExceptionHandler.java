package edu.hubu.grs.exception;

import edu.hubu.grs.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//全局异常处理
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handle(Exception e) {
        return Result.fail(e.getMessage());
    }
}
