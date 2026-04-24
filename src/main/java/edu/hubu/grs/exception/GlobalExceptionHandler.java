package edu.hubu.grs.exception;

import cn.dev33.satoken.exception.NotLoginException;
import edu.hubu.grs.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//全局异常处理
@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * 处理 SaToken 未登录异常（token 无效/过期/未传）
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<Result<String>> handleNotLogin(NotLoginException e) {
        // 返回 HTTP 401 状态码
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Result.fail("token 无效，请重新登录"));
    }
    
    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handle(Exception e) {
        return Result.fail(e.getMessage());
    }


}
