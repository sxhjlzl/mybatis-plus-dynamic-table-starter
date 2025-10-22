package com.lizhuolun.mybatis.dynamic.strategy.impl;

import com.lizhuolun.mybatis.dynamic.strategy.TableRouterStrategy;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 基于日期的分表策略
 * 支持按年、月、日进行分表
 *
 * @author 李卓伦
 * @date 2025/07/25 10:25
 */
@Slf4j
public class DateBasedTableRouterStrategy implements TableRouterStrategy {

    /**
     * 支持的逻辑表名集合
     **/
    private final Set<String> supportedTables;

    /**
     * 日期格式化器
     **/
    private final DateTimeFormatter formatter;

    /**
     * 策略优先级
     **/
    private final int priority;

    /**
     * 构造函数
     *
     * @param supportedTables 支持的表名集合
     * @param datePattern 日期格式模式（如：yyyyMM、yyyyMMdd）
     * @param priority 优先级
     * @author 李卓伦
     * @date 2025/07/25 10:26
     */
    public DateBasedTableRouterStrategy(Set<String> supportedTables, String datePattern, int priority) {
        this.supportedTables = supportedTables != null ? new HashSet<>(supportedTables) : new HashSet<>();
        this.formatter = DateTimeFormatter.ofPattern(datePattern != null ? datePattern : "yyyyMM");
        this.priority = priority;
        log.info("初始化基于日期的分表策略: 支持表={}, 日期格式={}, 优先级={}", 
                this.supportedTables, datePattern, priority);
    }

    /**
     * 构造函数（默认按月分表）
     *
     * @param supportedTables 支持的表名集合
     * @author 李卓伦
     * @date 2025/07/25 10:27
     */
    public DateBasedTableRouterStrategy(Set<String> supportedTables) {
        this(supportedTables, "yyyyMM", 50);
    }

    @Override
    public String getActualTableName(String logicTableName, Object context) {
        if (!match(logicTableName)) {
            log.warn("表名不匹配当前策略: {}", logicTableName);
            return logicTableName;
        }

        String dateSuffix = extractDateSuffix(context);
        if (dateSuffix == null) {
            log.warn("无法提取日期后缀，使用当前日期: context={}", context);
            dateSuffix = LocalDate.now().format(formatter);
        }

        String actualTableName = logicTableName + "_" + dateSuffix;
        log.debug("生成实际表名: {} -> {}", logicTableName, actualTableName);
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
        return "DateBasedTableRouterStrategy";
    }

    @Override
    public int getPriority() {
        return priority;
    }

    /**
     * 从上下文对象中提取日期后缀
     *
     * @param context 上下文对象
     * @return 日期后缀字符串
     * @author 李卓伦
     * @date 2025/07/25 10:28
     */
    private String extractDateSuffix(Object context) {
        if (context == null) {
            log.debug("上下文对象为空，无法提取日期后缀");
            return null;
        }

        try {
            if (context instanceof LocalDateTime) {
                return ((LocalDateTime) context).format(formatter);
            } else if (context instanceof LocalDate) {
                return ((LocalDate) context).format(formatter);
            } else if (context instanceof Date) {
                // 将Date转换为LocalDateTime
                Date date = (Date) context;
                LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), 
                        java.time.ZoneId.systemDefault());
                return localDateTime.format(formatter);
            } else if (context instanceof String) {
                return parseStringDate((String) context);
            } else if (context instanceof Number) {
                // 处理时间戳
                long timestamp = ((Number) context).longValue();
                LocalDateTime dateTime = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(timestamp), 
                        java.time.ZoneId.systemDefault());
                return dateTime.format(formatter);
            } else {
                log.warn("不支持的日期上下文类型: {}", context.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("解析日期上下文失败: context={}, type={}, error={}", 
                    context, context.getClass().getSimpleName(), e.getMessage(), e);
        }

        return null;
    }

    /**
     * 解析字符串日期
     *
     * @param dateStr 日期字符串
     * @return 格式化后的日期字符串
     * @author 李卓伦
     * @date 2025/07/25 10:29
     */
    private String parseStringDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        dateStr = dateStr.trim();
        
        try {
            // 匹配 yyyy-MM-dd 格式
            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                LocalDate date = LocalDate.parse(dateStr.substring(0, 10));
                return date.format(formatter);
            }
            // 匹配 yyyy/MM/dd 格式
            else if (dateStr.matches("\\d{4}/\\d{2}/\\d{2}.*")) {
                String normalizedDate = dateStr.substring(0, 10).replace("/", "-");
                LocalDate date = LocalDate.parse(normalizedDate);
                return date.format(formatter);
            }
            // 匹配纯数字格式 yyyyMMdd 或 yyyyMM
            else if (dateStr.matches("\\d{6,8}")) {
                if (dateStr.length() == 8) {
                    // yyyyMMdd 格式
                    LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
                    return date.format(formatter);
                } else if (dateStr.length() == 6) {
                    // yyyyMM 格式，默认为当月第一天
                    LocalDate date = LocalDate.parse(dateStr + "01", DateTimeFormatter.ofPattern("yyyyMMdd"));
                    return date.format(formatter);
                } else {
                    // 直接返回数字格式的日期
                    return dateStr;
                }
            }
            // 尝试ISO格式解析
            else if (dateStr.contains("T")) {
                LocalDateTime dateTime = LocalDateTime.parse(dateStr);
                return dateTime.format(formatter);
            }
        } catch (Exception e) {
            log.warn("解析字符串日期失败: dateStr={}, error={}", dateStr, e.getMessage());
        }

        return null;
    }

    /**
     * 添加支持的表名
     *
     * @param tableName 表名
     * @author 李卓伦
     * @date 2025/07/25 10:29
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
     * @date 2025/07/25 10:30
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
     * @date 2025/07/25 10:31
     */
    public Set<String> getSupportedTables() {
        return new HashSet<>(supportedTables);
    }
}