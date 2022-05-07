package com.gitee.olinonee.web.handler;

import com.gitee.olinonee.web.utils.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Optional;

/**
 * 全局异常统一处理器
 * <Strong>注意：</Strong>
 * <p>此方式适用于SpringBoot&MVC项目，由于集成SpringSecurity安全框架，统一的异常处理由安全框架过滤处理了。
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-29
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseResult<Object> handleError(UsernameNotFoundException e) {
        log.error("用户信息未找到异常，异常信息为 -> {}", e.getMessage());
        String message = String.format("用户信息未找到: %s", e.getMessage());
        return ResponseResult.failure(HttpStatus.FORBIDDEN, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseResult<Object> handleError(IllegalArgumentException e) {
        log.error("非法参数异常，异常信息为 -> {}", e.getMessage());
        String message = String.format("非法参数: %s", e.getMessage());
        return ResponseResult.failure(HttpStatus.FORBIDDEN, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Object> handleError(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数异常，异常信息为 -> {}", e.getMessage());
        String message = String.format("缺少必要的请求参数: %s", e.getParameterName());
        return ResponseResult.failure(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Object> handleError(MethodArgumentTypeMismatchException e) {
        log.warn("请求参数格式错误异常，异常信息为 -> {}", e.getMessage());
        String message = String.format("请求参数格式错误: %s", e.getName());
        return ResponseResult.failure(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Object> handleError(MethodArgumentNotValidException e) {
        log.warn("参数验证失败异常，异常信息为 -> {}", e.getMessage());
        return handleError(e.getBindingResult());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Object> handleError(BindException e) {
        log.warn("参数绑定失败异常，异常信息为 -> {}", e.getMessage());
        return handleError(e.getBindingResult());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseResult<Object> handleError(NoHandlerFoundException e) {
        log.error("404没找到请求异常，异常信息为 -> {}", e.getMessage());
        return ResponseResult.failure(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Object> handleError(HttpMessageNotReadableException e) {
        log.error("消息不能读取异常，异常信息为 -> {}", e.getMessage());
        return ResponseResult.failure(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseResult<Object> handleError(HttpRequestMethodNotSupportedException e) {
        log.error("不支持当前请求方法异常，异常信息为 -> {}", e.getMessage());
        return ResponseResult.failure(HttpStatus.METHOD_NOT_ALLOWED, e.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ResponseResult<Object> handleError(HttpMediaTypeNotSupportedException e) {
        log.error("不支持当前媒体类型异常，异常信息为 -> {}", e.getMessage());
        return ResponseResult.failure(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ResponseResult<Object> handleError(HttpMediaTypeNotAcceptableException e) {
        String message = e.getMessage() + " " + StringUtils.collectionToCommaDelimitedString(e.getSupportedMediaTypes());
        log.error("不接受的媒体类型异常，异常信息为 -> {}", message);
        return ResponseResult.failure(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message);
    }

    private ResponseResult<Object> handleError(BindingResult result) {
        FieldError fieldError = result.getFieldError();
        String message = Optional.ofNullable(fieldError)
                .map(error -> String.format("%s:%s", error.getField(), error.getDefaultMessage()))
                .orElse("参数绑定失败!");
        return ResponseResult.failure(HttpStatus.BAD_REQUEST, message);
    }
}
