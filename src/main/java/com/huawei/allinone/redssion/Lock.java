package com.huawei.allinone.redssion;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解类型实现分布式锁：@Lock
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Lock {

    String prefix() default "";

    String key() default "";

    long expire() default 60L;

    int tryCount() default 3;
} 