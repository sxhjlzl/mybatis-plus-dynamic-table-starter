package com.lizhuolun.mybatis.dynamic.annotation;

import java.lang.annotation.*;

/**
 * 动态表名注解
 * 用于标记需要动态表名处理的方法
 *
 * @author 李卓伦
 * @date 2025/01/25 10:00
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicTable {

    /**
     * 逻辑表名
     *
     * @return 逻辑表名
     */
    String value();

    /**
     * 分表策略类型
     *
     * @return 策略类型
     */
    StrategyType strategy() default StrategyType.AUTO;

    /**
     * 分表键参数名（用于从方法参数中获取分表键）
     *
     * @return 参数名
     */
    String shardingKey() default "";

    /**
     * 分表键参数索引（从0开始）
     *
     * @return 参数索引
     */
    int shardingKeyIndex() default 0;

    /**
     * 策略类型枚举
     *
     * @author 李卓伦
     * @date 2025/01/25 10:01
     */
    enum StrategyType {
        /**
         * 自动检测
         */
        AUTO,
        /**
         * 日期分表
         */
        DATE,
        /**
         * 哈希分表
         */
        HASH,
        /**
         * 自定义
         */
        CUSTOM
    }
}
