package com.lizhuolun.mybatis.dynamic.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态表名配置属性
 * 用于配置动态表名相关参数
 *
 * @author 李卓伦
 * @date 2025/07/25 10:55
 */
@Data
@Slf4j
@ConfigurationProperties(prefix = "dynamic-table")
public class DynamicTableProperties {

    /**
     * 是否启用动态表名功能
     **/
    private boolean enabled = true;

    /**
     * 是否启用SQL日志打印
     **/
    private boolean enableSqlLog = false;

    /**
     * 日期分表配置列表
     **/
    private List<DateShardingConfig> dateSharding = new ArrayList<>();

    /**
     * 哈希分表配置列表
     **/
    private List<HashShardingConfig> hashSharding = new ArrayList<>();

    /**
     * 表配置列表（新格式支持）
     **/
    private List<TableConfig> tables = new ArrayList<>();

    /**
     * 配置初始化后的验证
     *
     * @author 李卓伦
     * @date 2025/07/25 10:25
     */
    @PostConstruct
    public void validateConfig() {
        log.info("动态表名配置初始化: enabled={}, enableSqlLog={}", enabled, enableSqlLog);

        if (!enabled) {
            log.info("动态表名功能已禁用");
            return;
        }

        int totalConfigs = 0;

        if (dateSharding != null && !dateSharding.isEmpty()) {
            totalConfigs += dateSharding.size();
            log.info("日期分表配置数量: {}", dateSharding.size());
            validateDateShardingConfig();
        }

        if (hashSharding != null && !hashSharding.isEmpty()) {
            totalConfigs += hashSharding.size();
            log.info("哈希分表配置数量: {}", hashSharding.size());
            validateHashShardingConfig();
        }

        if (tables != null && !tables.isEmpty()) {
            totalConfigs += tables.size();
            log.info("表配置数量: {}", tables.size());
            validateTableConfig();
        }

        if (totalConfigs == 0) {
            log.warn("未配置任何分表策略，动态表名功能将不会生效");
        } else {
            log.info("动态表名配置验证完成，共配置{}个分表策略", totalConfigs);
        }
    }

    /**
     * 验证日期分表配置
     *
     * @author 李卓伦
     * @date 2025/07/25 10:26
     */
    private void validateDateShardingConfig() {
        for (int i = 0; i < dateSharding.size(); i++) {
            DateShardingConfig config = dateSharding.get(i);
            if (config.getTables() == null || config.getTables().isEmpty()) {
                log.warn("日期分表配置[{}]的表名列表为空", i);
            }
            if (config.getDatePattern() == null || config.getDatePattern().trim().isEmpty()) {
                log.warn("日期分表配置[{}]的日期格式为空", i);
            }
        }
    }

    /**
     * 验证哈希分表配置
     *
     * @author 李卓伦
     * @date 2025/07/25 10:27
     */
    private void validateHashShardingConfig() {
        for (int i = 0; i < hashSharding.size(); i++) {
            HashShardingConfig config = hashSharding.get(i);
            if (config.getTables() == null || config.getTables().isEmpty()) {
                log.warn("哈希分表配置[{}]的表名列表为空", i);
            }
            if (config.getTableCount() <= 0) {
                log.warn("哈希分表配置[{}]的表数量无效: {}", i, config.getTableCount());
            }
        }
    }

    /**
     * 验证表配置
     *
     * @author 李卓伦
     * @date 2025/07/25 10:28
     */
    private void validateTableConfig() {
        for (int i = 0; i < tables.size(); i++) {
            TableConfig config = tables.get(i);
            if (config.getTableName() == null || config.getTableName().trim().isEmpty()) {
                log.warn("表配置[{}]的表名为空", i);
            }
            if (config.getStrategy() == null || config.getStrategy().trim().isEmpty()) {
                log.warn("表配置[{}]的策略为空", i);
            }
        }
    }

    /**
     * 日期分表配置
     *
     * @author 李卓伦
     * @date 2025/07/25 10:56
     */
    @Data
    public static class DateShardingConfig {

        /**
         * 逻辑表名列表
         **/
        private List<String> tables = new ArrayList<>();

        /**
         * 日期格式模式（如：yyyyMM、yyyyMMdd）
         **/
        private String datePattern = "yyyyMM";

        /**
         * 策略优先级
         **/
        private int priority = 50;
    }

    /**
     * 哈希分表配置
     *
     * @author 李卓伦
     * @date 2025/07/25 10:57
     */
    @Data
    public static class HashShardingConfig {

        /**
         * 逻辑表名列表
         **/
        private List<String> tables = new ArrayList<>();

        /**
         * 分表数量
         **/
        private int tableCount = 8;

        /**
         * 策略优先级
         **/
        private int priority = 60;
    }

    /**
     * 表配置内部类（新格式支持）
     *
     * @author 李卓伦
     * @date 2025/07/25 15:45
     */
    @Data
    public static class TableConfig {

        /**
         * 表名
         **/
        private String tableName;

        /**
         * 分表策略（DATE、HASH）
         **/
        private String strategy;

        /**
         * 日期格式（用于日期分表策略）
         **/
        private String format = "yyyyMM";

        /**
         * 取模值（用于哈希分表策略）
         **/
        private Integer modValue = 10;

        /**
         * 策略优先级
         **/
        private Integer priority = 100;
    }
}