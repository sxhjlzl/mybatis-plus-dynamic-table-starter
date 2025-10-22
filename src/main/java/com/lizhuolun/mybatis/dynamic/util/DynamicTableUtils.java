package com.lizhuolun.mybatis.dynamic.util;

import com.lizhuolun.mybatis.dynamic.context.DynamicTableContextHolder;
import com.lizhuolun.mybatis.dynamic.strategy.TableRouterStrategy;
import com.lizhuolun.mybatis.dynamic.strategy.TableRouterStrategyFactory;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 动态表名工具类
 * 提供便捷的动态表名操作方法
 *
 * @author 李卓伦
 * @date 2025/07/25 11:10
 */
@Slf4j
public class DynamicTableUtils {

    /**
     * 私有构造函数，防止实例化
     *
     * @author 李卓伦
     * @date 2025/07/25 11:11
     */
    private DynamicTableUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 设置表名映射并执行操作
     *
     * @param logicTable 逻辑表名
     * @param actualTable 实际表名
     * @param operation 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     * @author 李卓伦
     * @date 2025/07/25 11:12
     */
    public static <T> T executeWithTable(String logicTable, String actualTable, Supplier<T> operation) {
        if (logicTable == null || actualTable == null || operation == null) {
            log.warn("执行表名映射操作失败，参数不能为空: logicTable={}, actualTable={}", logicTable, actualTable);
            throw new IllegalArgumentException("表名映射操作的参数不能为空");
        }
        
        try {
            DynamicTableContextHolder.set(logicTable, actualTable);
            log.debug("设置表名映射并执行操作: {} -> {}", logicTable, actualTable);
            return operation.get();
        } catch (Exception e) {
            log.error("执行表名映射操作时发生异常: logicTable={}, actualTable={}, error={}", 
                    logicTable, actualTable, e.getMessage(), e);
            throw e;
        } finally {
            DynamicTableContextHolder.remove(logicTable);
            log.debug("清理表名映射: {}", logicTable);
        }
    }

    /**
     * 设置多个表名映射并执行操作
     *
     * @param tableMap 表名映射集合
     * @param operation 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     * @author 李卓伦
     * @date 2025/07/25 11:13
     */
    public static <T> T executeWithTables(Map<String, String> tableMap, Supplier<T> operation) {
        if (tableMap == null || tableMap.isEmpty() || operation == null) {
            log.warn("执行多表映射操作失败，参数不能为空或空集合");
            throw new IllegalArgumentException("多表映射操作的参数不能为空或空集合");
        }
        
        try {
            DynamicTableContextHolder.setAll(tableMap);
            log.debug("设置多个表名映射并执行操作: {}", tableMap);
            return operation.get();
        } catch (Exception e) {
            log.error("执行多表映射操作时发生异常: tableMap={}, error={}", 
                    tableMap, e.getMessage(), e);
            throw e;
        } finally {
            for (String logicTable : tableMap.keySet()) {
                DynamicTableContextHolder.remove(logicTable);
            }
            log.debug("清理多个表名映射: {}", tableMap.keySet());
        }
    }

    /**
     * 使用策略设置表名映射并执行操作
     *
     * @param logicTable 逻辑表名
     * @param context 上下文对象
     * @param operation 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     * @author 李卓伦
     * @date 2025/07/25 11:14
     */
    public static <T> T executeWithStrategy(String logicTable, Object context, Supplier<T> operation) {
        TableRouterStrategy strategy = TableRouterStrategyFactory.getStrategy(logicTable);
        if (strategy == null) {
            log.warn("未找到匹配的分表策略: {}", logicTable);
            return operation.get();
        }

        String actualTable = strategy.getActualTableName(logicTable, context);
        return executeWithTable(logicTable, actualTable, operation);
    }

    /**
     * 基于日期设置表名映射并执行操作
     *
     * @param logicTable 逻辑表名
     * @param date 日期
     * @param operation 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     * @author 李卓伦
     * @date 2025/07/25 11:15
     */
    public static <T> T executeWithDate(String logicTable, LocalDate date, Supplier<T> operation) {
        return executeWithStrategy(logicTable, date, operation);
    }

    /**
     * 基于日期时间设置表名映射并执行操作
     *
     * @param logicTable 逻辑表名
     * @param dateTime 日期时间
     * @param operation 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     * @author 李卓伦
     * @date 2025/07/25 11:16
     */
    public static <T> T executeWithDateTime(String logicTable, LocalDateTime dateTime, Supplier<T> operation) {
        return executeWithStrategy(logicTable, dateTime, operation);
    }

    /**
     * 基于哈希值设置表名映射并执行操作
     *
     * @param logicTable 逻辑表名
     * @param hashKey 哈希键
     * @param operation 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     * @author 李卓伦
     * @date 2025/07/25 11:17
     */
    public static <T> T executeWithHash(String logicTable, String hashKey, Supplier<T> operation) {
        return executeWithStrategy(logicTable, hashKey, operation);
    }

    /**
     * 执行无返回值的操作
     *
     * @param logicTable 逻辑表名
     * @param actualTable 实际表名
     * @param operation 要执行的操作
     * @author 李卓伦
     * @date 2025/07/25 11:18
     */
    public static void executeWithTable(String logicTable, String actualTable, Runnable operation) {
        executeWithTable(logicTable, actualTable, () -> {
            operation.run();
            return null;
        });
    }

    /**
     * 执行无返回值的操作（多表）
     *
     * @param tableMap 表名映射集合
     * @param operation 要执行的操作
     * @author 李卓伦
     * @date 2025/07/25 11:19
     */
    public static void executeWithTables(Map<String, String> tableMap, Runnable operation) {
        executeWithTables(tableMap, () -> {
            operation.run();
            return null;
        });
    }

    /**
     * 执行无返回值的操作（策略）
     *
     * @param logicTable 逻辑表名
     * @param context 上下文对象
     * @param operation 要执行的操作
     * @author 李卓伦
     * @date 2025/07/25 11:20
     */
    public static void executeWithStrategy(String logicTable, Object context, Runnable operation) {
        executeWithStrategy(logicTable, context, () -> {
            operation.run();
            return null;
        });
    }

    /**
     * 获取实际表名（通过策略）
     *
     * @param logicTable 逻辑表名
     * @param context 上下文对象
     * @return 实际表名
     * @author 李卓伦
     * @date 2025/07/25 11:21
     */
    public static String getActualTableName(String logicTable, Object context) {
        TableRouterStrategy strategy = TableRouterStrategyFactory.getStrategy(logicTable);
        if (strategy == null) {
            log.warn("未找到匹配的分表策略，返回原表名: {}", logicTable);
            return logicTable;
        }
        return strategy.getActualTableName(logicTable, context);
    }

    /**
     * 检查是否存在指定表的策略
     *
     * @param logicTable 逻辑表名
     * @return 是否存在策略
     * @author 李卓伦
     * @date 2025/07/25 11:22
     */
    public static boolean hasStrategy(String logicTable) {
        return TableRouterStrategyFactory.getStrategy(logicTable) != null;
    }

    /**
     * 获取当前线程的表名映射信息
     *
     * @return 表名映射集合
     * @author 李卓伦
     * @date 2025/07/25 11:23
     */
    public static Map<String, String> getCurrentTableMappings() {
        return DynamicTableContextHolder.getAll();
    }

    /**
     * 清理当前线程的所有表名映射
     *
     * @author 李卓伦
     * @date 2025/07/25 11:24
     */
    public static void clearCurrentMappings() {
        DynamicTableContextHolder.clear();
        log.debug("清理当前线程的所有表名映射");
    }
}