package com.aining.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 基于 Redis + 滑动窗口的接口限流注解，支持 SpEL 表达式动态生成限流 key。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 限流 key 前缀 */
    String prefix() default "rate:";

    /** SpEL 表达式，用于动态构建限流 key 后缀，例如 "#request.getHeader('X-Openid')" */
    String key() default "";

    /** 时间窗口，单位秒 */
    int timeWindow() default 60;

    /** 时间窗口内最大允许请求数 */
    int maxRequests() default 10;

    /** 触发限流后的提示信息 */
    String message() default "请求过于频繁，请稍后重试";
}
