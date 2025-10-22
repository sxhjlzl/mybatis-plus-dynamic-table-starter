package com.lizhuolun.mybatis.dynamic.context;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 动态表名上下文管理器
 * 使用ThreadLocal管理当前线程的表名映射关系
 *
 * @author 李卓伦
 * @date 2025/07/25 10:00
 */
@Slf4j
public class DynamicTableContextHolder {

    /**
     * 线程本地变量，存储逻辑表名到实际表名的映射
     **/
    private static final ThreadLocal<Map<String, String>> TABLE_MAP = ThreadLocal.withInitial(HashMap::new);

    /**
     * 设置表名映射
     *
     * @param logicTable 逻辑表名
     * @param actualTable 实际表名
     * @author 李卓伦
     * @date 2025/07/25 10:01
     */
    public static void set(String logicTable, String actualTable) {
        if (logicTable == null || actualTable == null) {
            log.warn("设置表名映射失败，参数不能为空: logicTable={}, actualTable={}", logicTable, actualTable);
            return;
        }
        TABLE_MAP.get().put(logicTable, actualTable);
        log.debug("设置表名映射: {} -> {}", logicTable, actualTable);
    }

    /**
     * 获取实际表名
     *
     * @param logicTable 逻辑表名
     * @return 实际表名，如果未找到则返回null
     * @author 李卓伦
     * @date 2025/07/25 10:02
     */
    public static String get(String logicTable) {
        String actualTable = TABLE_MAP.get().get(logicTable);
        log.debug("获取表名映射: {} -> {}", logicTable, actualTable);
        return actualTable;
    }

    /**
     * 批量设置表名映射
     *
     * @param tableMap 表名映射集合
     * @author 李卓伦
     * @date 2025/07/25 10:03
     */
    public static void setAll(Map<String, String> tableMap) {
        if (tableMap == null || tableMap.isEmpty()) {
            log.warn("批量设置表名映射失败，参数不能为空");
            return;
        }
        TABLE_MAP.get().putAll(tableMap);
        log.debug("批量设置表名映射: {}", tableMap);
    }

    /**
     * 获取当前线程的所有表名映射
     *
     * @return 表名映射集合
     * @author 李卓伦
     * @date 2025/07/25 10:04
     */
    public static Map<String, String> getAll() {
        return new HashMap<>(TABLE_MAP.get());
    }

    /**
     * 移除指定的表名映射
     *
     * @param logicTable 逻辑表名
     * @author 李卓伦
     * @date 2025/07/25 10:05
     */
    public static void remove(String logicTable) {
        if (logicTable == null) {
            log.warn("移除表名映射失败，逻辑表名不能为空");
            return;
        }
        String removed = TABLE_MAP.get().remove(logicTable);
        log.debug("移除表名映射: {} -> {}", logicTable, removed);
    }

    /**
     * 清空当前线程的所有表名映射
     *
     * @author 李卓伦
     * @date 2025/07/25 10:06
     */
    public static void clear() {
        Map<String, String> tableMap = TABLE_MAP.get();
        if (!tableMap.isEmpty()) {
            log.debug("清空表名映射: {}", tableMap);
            tableMap.clear();
        }
        TABLE_MAP.remove();
    }

    /**
     * 检查是否存在指定的表名映射
     *
     * @param logicTable 逻辑表名
     * @return 是否存在映射
     * @author 李卓伦
     * @date 2025/07/25 10:07
     */
    public static boolean contains(String logicTable) {
        return logicTable != null && TABLE_MAP.get().containsKey(logicTable);
    }

    /**
     * 获取当前映射数量
     *
     * @return 映射数量
     * @author 李卓伦
     * @date 2025/07/25 10:08
     */
    public static int size() {
        return TABLE_MAP.get().size();
    }
}