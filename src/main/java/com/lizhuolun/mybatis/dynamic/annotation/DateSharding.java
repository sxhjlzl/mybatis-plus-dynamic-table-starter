package com.lizhuolun.mybatis.dynamic.annotation;

import java.lang.annotation.*;

/**
 * 日期分表注解
 * 用于标记基于日期分表的方法
 *
 * @author 李卓伦
 * @date 2025/01/25 10:02
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DateSharding {

    /**
     * 逻辑表名
     *
     * @return 逻辑表名
     */
    String value();

    /**
     * 日期参数名
     *
     * @return 参数名
     */
    String dateParam() default "date";

    /**
     * 日期参数索引（从0开始）
     *
     * @return 参数索引
     */
    int dateIndex() default 0;
}
