package com.lizhuolun.mybatis.dynamic.strategy;

/**
 * 分表策略接口
 * 定义分表路由规则，支持多种分表策略实现
 *
 * @author 李卓伦
 * @date 2025/07/25 10:10
 */
public interface TableRouterStrategy {

    /**
     * 根据条件生成实际表名
     *
     * @param logicTableName 逻辑表名（如 order）
     * @param context 条件对象（可扩展：时间、医院、渠道等）
     * @return 实际表名
     * @author 李卓伦
     * @date 2025/07/25 10:11
     */
    String getActualTableName(String logicTableName, Object context);

    /**
     * 是否匹配当前策略
     *
     * @param logicTableName 逻辑表名
     * @return 是否匹配
     * @author 李卓伦
     * @date 2025/07/25 10:12
     */
    boolean match(String logicTableName);

    /**
     * 获取策略名称
     *
     * @return 策略名称
     * @author 李卓伦
     * @date 2025/07/25 10:13
     */
    default String getStrategyName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 获取策略优先级，数值越小优先级越高
     *
     * @return 优先级
     * @author 李卓伦
     * @date 2025/07/25 10:14
     */
    default int getPriority() {
        return 100;
    }
}