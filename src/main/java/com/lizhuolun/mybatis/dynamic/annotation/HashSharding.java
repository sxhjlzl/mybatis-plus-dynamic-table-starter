package com.lizhuolun.mybatis.dynamic.annotation;

import java.lang.annotation.*;

/**
 * 哈希分表注解
 * 用于标记基于哈希分表的方法
 *
 * @author 李卓伦
 * @date 2025/01/25 10:03
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HashSharding {

    /**
     * 逻辑表名
     *
     * @return 逻辑表名
     */
    String value();

    /**
     * 哈希键参数名
     *
     * @return 参数名
     */
    String hashKey() default "hashKey";

    /**
     * 哈希键参数索引（从0开始）
     *
     * @return 参数索引
     */
    int hashKeyIndex() default 0;
}
