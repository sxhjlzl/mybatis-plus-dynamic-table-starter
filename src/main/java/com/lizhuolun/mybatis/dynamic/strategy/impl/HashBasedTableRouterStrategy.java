package com.lizhuolun.mybatis.dynamic.strategy.impl;

import com.lizhuolun.mybatis.dynamic.strategy.TableRouterStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * 基于哈希的分表策略
 * 根据指定字段的哈希值进行分表
 *
 * @author 李卓伦
 * @date 2025/07/25 10:35
 */
@Slf4j
public class HashBasedTableRouterStrategy implements TableRouterStrategy {

    /**
     * 支持的逻辑表名集合
     **/
    private final Set<String> supportedTables;

    /**
     * 分表数量
     **/
    private final int tableCount;

    /**
     * 策略优先级
     **/
    private final int priority;

    /**
     * 构造函数
     *
     * @param supportedTables 支持的表名集合
     * @param tableCount 分表数量
     * @param priority 优先级
     * @author 李卓伦
     * @date 2025/07/25 10:36
     */
    public HashBasedTableRouterStrategy(Set<String> supportedTables, int tableCount, int priority) {
        this.supportedTables = supportedTables != null ? new HashSet<>(supportedTables) : new HashSet<>();
        this.tableCount = Math.max(1, tableCount);
        this.priority = priority;
        log.info("初始化基于哈希的分表策略: 支持表={}, 分表数量={}, 优先级={}", 
                this.supportedTables, this.tableCount, priority);
    }

    /**
     * 构造函数（默认分8张表）
     *
     * @param supportedTables 支持的表名集合
     * @author 李卓伦
     * @date 2025/07/25 10:37
     */
    public HashBasedTableRouterStrategy(Set<String> supportedTables) {
        this(supportedTables, 8, 60);
    }

    @Override
    public String getActualTableName(String logicTableName, Object context) {
        if (!match(logicTableName)) {
            log.warn("表名不匹配当前策略: {}", logicTableName);
            return logicTableName;
        }

        int hashSuffix = calculateHashSuffix(context);
        String actualTableName = logicTableName + "_" + hashSuffix;
        log.debug("生成实际表名: {} -> {} (context={}, hash={})", 
                logicTableName, actualTableName, context, hashSuffix);
        return actualTableName;
    }

    @Override
    public boolean match(String logicTableName) {
        boolean matches = logicTableName != null && supportedTables.contains(logicTableName);
        log.debug("表名匹配检查: {} -> {}", logicTableName, matches);
        return matches;
    }

    @Override
    public String getStrategyName() {
        return "HashBasedTableRouterStrategy";
    }

    @Override
    public int getPriority() {
        return priority;
    }

    /**
     * 计算哈希后缀
     *
     * @param context 上下文对象
     * @return 哈希后缀（0到tableCount-1）
     * @author 李卓伦
     * @date 2025/07/25 10:38
     */
    private int calculateHashSuffix(Object context) {
        if (context == null) {
            log.warn("上下文对象为空，使用默认哈希值0");
            return 0;
        }

        try {
            String hashKey = extractHashKey(context);
            if (hashKey == null || hashKey.isEmpty()) {
                log.warn("无法提取哈希键，使用默认哈希值0: context={}", context);
                return 0;
            }

            // 使用改进的哈希算法，提高分布均匀性
            int hash = hashKey.hashCode();
            // 处理负数情况，避免使用Math.abs可能导致的Integer.MIN_VALUE问题
            if (hash == Integer.MIN_VALUE) {
                hash = 0;
            } else if (hash < 0) {
                hash = Math.abs(hash);
            }
            
            int suffix = hash % tableCount;
            
            log.debug("哈希计算: key={}, hash={}, suffix={}, tableCount={}", 
                    hashKey, hash, suffix, tableCount);
            return suffix;
        } catch (Exception e) {
            log.error("计算哈希后缀失败，使用默认值0: context={}, error={}", 
                    context, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 从上下文对象中提取哈希键
     *
     * @param context 上下文对象
     * @return 哈希键字符串
     * @author 李卓伦
     * @date 2025/07/25 10:39
     */
    private String extractHashKey(Object context) {
        if (context == null) {
            return null;
        }

        if (context instanceof String) {
            return (String) context;
        } else if (context instanceof Number) {
            return context.toString();
        } else {
            // 对于复杂对象，尝试使用toString方法
            return context.toString();
        }
    }

    /**
     * 添加支持的表名
     *
     * @param tableName 表名
     * @author 李卓伦
     * @date 2025/07/25 10:40
     */
    public void addSupportedTable(String tableName) {
        if (tableName != null && !tableName.trim().isEmpty()) {
            supportedTables.add(tableName);
            log.debug("添加支持的表名: {}", tableName);
        }
    }

    /**
     * 移除支持的表名
     *
     * @param tableName 表名
     * @author 李卓伦
     * @date 2025/07/25 10:41
     */
    public void removeSupportedTable(String tableName) {
        if (supportedTables.remove(tableName)) {
            log.debug("移除支持的表名: {}", tableName);
        }
    }

    /**
     * 获取支持的表名集合
     *
     * @return 表名集合副本
     * @author 李卓伦
     * @date 2025/07/25 10:42
     */
    public Set<String> getSupportedTables() {
        return new HashSet<>(supportedTables);
    }

    /**
     * 获取分表数量
     *
     * @return 分表数量
     * @author 李卓伦
     * @date 2025/07/25 10:43
     */
    public int getTableCount() {
        return tableCount;
    }
}