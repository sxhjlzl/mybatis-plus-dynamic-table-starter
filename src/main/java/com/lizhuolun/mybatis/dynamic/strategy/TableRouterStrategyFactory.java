package com.lizhuolun.mybatis.dynamic.strategy;

import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 分表策略工厂
 * 管理和注册所有分表策略，支持策略优先级排序
 *
 * @author 李卓伦
 * @date 2025/07/25 10:15
 */
@Slf4j
public class TableRouterStrategyFactory {

    /**
     * 策略列表，使用线程安全的CopyOnWriteArrayList
     **/
    private static final List<TableRouterStrategy> STRATEGIES = new CopyOnWriteArrayList<>();

    /**
     * 注册分表策略
     *
     * @param strategy 分表策略实例
     * @author 李卓伦
     * @date 2025/07/25 10:16
     */
    public static void register(TableRouterStrategy strategy) {
        if (strategy == null) {
            log.warn("注册分表策略失败，策略实例不能为空");
            return;
        }

        try {
            // 验证策略名称
            String strategyName = strategy.getStrategyName();
            if (strategyName == null || strategyName.trim().isEmpty()) {
                log.warn("注册分表策略失败，策略名称不能为空: {}", strategy.getClass().getSimpleName());
                return;
            }

            // 检查是否已存在相同策略名称
            boolean nameExists = STRATEGIES.stream()
                    .anyMatch(s -> strategyName.equals(s.getStrategyName()));
            
            if (nameExists) {
                log.warn("策略名称已存在，跳过注册: {}", strategyName);
                return;
            }

            // 检查是否已存在相同类型的策略
            boolean typeExists = STRATEGIES.stream()
                    .anyMatch(s -> s.getClass().equals(strategy.getClass()));
            
            if (typeExists) {
                log.warn("策略类型已存在，跳过注册: {}", strategy.getClass().getSimpleName());
                return;
            }

            STRATEGIES.add(strategy);
            
            // 按优先级排序
            STRATEGIES.sort(Comparator.comparingInt(TableRouterStrategy::getPriority));
            
            log.info("注册分表策略成功: {}, 类型: {}, 优先级: {}", 
                    strategyName, strategy.getClass().getSimpleName(), strategy.getPriority());
        } catch (Exception e) {
            log.error("注册分表策略异常: {}, error: {}", 
                    strategy.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    /**
     * 批量注册分表策略
     *
     * @param strategies 策略列表
     * @author 李卓伦
     * @date 2025/07/25 10:17
     */
    public static void registerAll(List<TableRouterStrategy> strategies) {
        if (strategies == null || strategies.isEmpty()) {
            log.warn("批量注册分表策略失败，策略列表不能为空");
            return;
        }

        for (TableRouterStrategy strategy : strategies) {
            register(strategy);
        }
    }

    /**
     * 根据逻辑表名获取匹配的策略
     *
     * @param logicTableName 逻辑表名
     * @return 匹配的策略，如果没有找到则返回null
     * @author 李卓伦
     * @date 2025/07/25 10:18
     */
    public static TableRouterStrategy getStrategy(String logicTableName) {
        if (logicTableName == null || logicTableName.trim().isEmpty()) {
            log.warn("获取分表策略失败，逻辑表名不能为空");
            return null;
        }

        TableRouterStrategy matchedStrategy = STRATEGIES.stream()
                .filter(s -> s.match(logicTableName))
                .findFirst()
                .orElse(null);

        if (matchedStrategy != null) {
            log.debug("找到匹配的分表策略: {} -> {}", logicTableName, matchedStrategy.getStrategyName());
        } else {
            log.debug("未找到匹配的分表策略: {}", logicTableName);
        }

        return matchedStrategy;
    }

    /**
     * 获取所有已注册的策略
     *
     * @return 策略列表副本
     * @author 李卓伦
     * @date 2025/07/25 10:19
     */
    public static List<TableRouterStrategy> getAllStrategies() {
        return new CopyOnWriteArrayList<>(STRATEGIES);
    }

    /**
     * 移除指定策略
     *
     * @param strategyClass 策略类
     * @author 李卓伦
     * @date 2025/07/25 10:20
     */
    public static void removeStrategy(Class<? extends TableRouterStrategy> strategyClass) {
        if (strategyClass == null) {
            log.warn("移除分表策略失败，策略类不能为空");
            return;
        }

        boolean removed = STRATEGIES.removeIf(s -> s.getClass().equals(strategyClass));
        if (removed) {
            log.info("移除分表策略成功: {}", strategyClass.getSimpleName());
        } else {
            log.warn("移除分表策略失败，未找到策略: {}", strategyClass.getSimpleName());
        }
    }

    /**
     * 清空所有策略
     *
     * @author 李卓伦
     * @date 2025/07/25 10:21
     */
    public static void clear() {
        int size = STRATEGIES.size();
        STRATEGIES.clear();
        log.info("清空所有分表策略，共移除{}个策略", size);
    }

    /**
     * 获取已注册策略数量
     *
     * @return 策略数量
     * @author 李卓伦
     * @date 2025/07/25 10:22
     */
    public static int getStrategyCount() {
        return STRATEGIES.size();
    }

    /**
     * 检查是否存在指定逻辑表名的策略
     *
     * @param logicTableName 逻辑表名
     * @return 是否存在匹配的策略
     * @author 李卓伦
     * @date 2025/07/25 10:23
     */
    public static boolean hasStrategy(String logicTableName) {
        if (logicTableName == null || logicTableName.trim().isEmpty()) {
            return false;
        }

        return STRATEGIES.stream()
                .anyMatch(s -> s.match(logicTableName));
    }

    /**
     * 获取策略信息摘要
     *
     * @return 策略信息字符串
     * @author 李卓伦
     * @date 2025/07/25 10:24
     */
    public static String getStrategyInfo() {
        if (STRATEGIES.isEmpty()) {
            return "未注册任何分表策略";
        }

        StringBuilder info = new StringBuilder();
        info.append("已注册分表策略(").append(STRATEGIES.size()).append("个):\n");
        
        for (int i = 0; i < STRATEGIES.size(); i++) {
            TableRouterStrategy strategy = STRATEGIES.get(i);
            info.append(String.format("  %d. %s (类型: %s, 优先级: %d)\n", 
                    i + 1, 
                    strategy.getStrategyName(), 
                    strategy.getClass().getSimpleName(), 
                    strategy.getPriority()));
        }
        
        return info.toString();
    }
}