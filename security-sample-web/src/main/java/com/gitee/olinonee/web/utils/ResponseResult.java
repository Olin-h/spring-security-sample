package com.gitee.olinonee.web.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.Optional;

/**
 * 统一响应状态结果封装类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-23
 */
@Getter
@Setter
@ToString
// 以下注解表示：实体类与json互转的时候 属性值为null的不参与序列化
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private int code;
    /**
     * 状态值，是否成功
     */
    private boolean success;
    /**
     * 提示消息，如果有错误时，前端可以获取该字段进行提示
     */
    private String msg;
    /**
     * 结果数据
     */
    private T data;

    // -------------------------私有构造方法-------------------------
    private ResponseResult(HttpStatus httpStatus) {
        this(httpStatus, httpStatus.getReasonPhrase(), null);
    }

    private ResponseResult(HttpStatus httpStatus, String msg) {
        this(httpStatus, msg, null);
    }

    private ResponseResult(HttpStatus httpStatus, T data) {
        this(httpStatus, httpStatus.getReasonPhrase(), data);
    }

    private ResponseResult(HttpStatus httpStatus, String msg, T data) {
        this(httpStatus.value(), msg, data);
    }

    private ResponseResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.success = HttpStatus.OK.value() == code;
    }
    // -------------------------私有构造方法结束-------------------------


    /**
     * 判断返回是否为成功
     *
     * @param result 统一响应结果对象
     * @return 是否成功，为空或者操作失败返回false，操作成功返回true
     */
    public static boolean isSuccess(@Nullable ResponseResult<?> result) {
        return Optional
                .ofNullable(result)
                .map(r -> ObjectUtils.nullSafeEquals(HttpStatus.OK.value(), r.code))
                .orElse(Boolean.FALSE);
    }

    /**
     * 判断返回是否为失败
     *
     * @param result 统一响应结果对象
     * @return 是否成功，为空或者操作失败返回false，操作成功返回true
     */
    public static boolean isFailure(@Nullable ResponseResult<?> result) {
        return !ResponseResult.isSuccess(result);
    }

    /**
     * 返回包含结果数据的统一响应结果（状态码默认为200，提示消息为“操作成功”）
     *
     * @param data 结果数据
     * @param <T>  T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> data(T data) {
        return data("操作成功", data);
    }

    /**
     * 返回包含结果数据的统一响应结果（状态码默认为200，自定义提示消息）
     *
     * @param msg  提示消息
     * @param data 结果数据
     * @param <T>  T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> data(String msg, T data) {
        return data(HttpStatus.OK.value(), msg, data);
    }

    /**
     * 返回统一响应结果（自定义状态码，判断返回数据是否为空，为空默认显示“暂无数据”，不为空自定义提示消息）
     *
     * @param code 状态码
     * @param msg  提示消息
     * @param data 结果数据
     * @param <T>  T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> data(int code, String msg, T data) {
        return new ResponseResult<>(code, data == null ? "暂无数据" : msg, data);
    }

    /**
     * 返回统一【操作成功】响应结果（状态码默认为200，自定义提示消息）
     *
     * @param msg 提示消息
     * @param <T> T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> success(String msg) {
        return new ResponseResult<>(HttpStatus.OK, msg);
    }

    /**
     * 返回统一【操作成功】响应结果（自定义状态码）
     *
     * @param httpStatus 状态码枚举
     * @param <T>        T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> success(HttpStatus httpStatus) {
        return new ResponseResult<>(httpStatus);
    }

    /**
     * 返回统一【操作成功】响应结果（自定义状态码和提示消息）
     *
     * @param httpStatus 状态码枚举
     * @param msg        提示消息
     * @param <T>        T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> success(HttpStatus httpStatus, String msg) {
        return new ResponseResult<>(httpStatus, msg);
    }

    /**
     * 返回统一【操作失败】响应结果（状态码默认为400，自定义提示消息）
     *
     * @param msg 提示消息
     * @param <T> T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> failure(String msg) {
        return new ResponseResult<>(HttpStatus.BAD_REQUEST, msg);
    }

    /**
     * 返回统一【操作失败】响应结果（自定义状态码和提示消息）
     *
     * @param code 状态码
     * @param msg  提示消息
     * @param <T>  T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> failure(int code, String msg) {
        return new ResponseResult<>(code, msg, null);
    }

    /**
     * 返回统一【操作失败】响应结果（自定义状态码）
     *
     * @param httpStatus 状态码枚举
     * @param <T>        T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> failure(HttpStatus httpStatus) {
        return new ResponseResult<>(httpStatus);
    }

    /**
     * 返回统一【操作失败】响应结果（自定义状态码和提示消息）
     *
     * @param httpStatus 状态码枚举
     * @param msg        提示消息
     * @param <T>        T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> failure(HttpStatus httpStatus, String msg) {
        return new ResponseResult<>(httpStatus, msg);
    }

    /**
     * 返回统一【成功或者失败】响应结果（根据布尔类型，为true返回“操作成功”响应结果；反之，返回false对应响应结果）
     *
     * @param flag 状态标记，布尔类型值
     * @param <T>  T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> status(boolean flag) {
        return flag ? success("操作成功") : failure("操作失败");
    }
}
