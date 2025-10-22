package com.lizhuolun.mybatis.dynamic.interceptor;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.lizhuolun.mybatis.dynamic.context.DynamicTableContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.function.BiFunction;

/**
 * 动态表名内部拦截器
 * 继承MyBatis-Plus的InnerInterceptor，实现动态表名替换功能
 *
 * @author 李卓伦
 * @date 2025/07/25 10:45
 */
@Slf4j
public class DynamicTableNameInnerInterceptor implements InnerInterceptor {

    /**
     * 表名处理器函数
     **/
    private BiFunction<String, String, String> tableNameHandler;

    /**
     * 构造函数
     *
     * @author 李卓伦
     * @date 2025/07/25 10:46
     */
    public DynamicTableNameInnerInterceptor() {
        // 默认的表名处理器，从ThreadLocal中获取实际表名
        this.tableNameHandler = (sql, tableName) -> {
            String actualTableName = DynamicTableContextHolder.get(tableName);
            if (actualTableName != null && !actualTableName.equals(tableName)) {
                log.debug("动态表名替换: {} -> {}", tableName, actualTableName);
                return actualTableName;
            }
            return tableName;
        };
    }

    /**
     * 构造函数
     *
     * @param tableNameHandler 自定义表名处理器
     * @author 李卓伦
     * @date 2025/07/25 10:47
     */
    public DynamicTableNameInnerInterceptor(BiFunction<String, String, String> tableNameHandler) {
        this.tableNameHandler = tableNameHandler != null ? tableNameHandler : this.tableNameHandler;
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        // 在查询前进行表名替换
        processTableName(ms, boundSql);
    }

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException {
        // 在更新前进行表名替换
        BoundSql boundSql = ms.getBoundSql(parameter);
        processTableName(ms, boundSql);
    }

    /**
     * 处理表名替换
     *
     * @param ms       MappedStatement对象
     * @param boundSql BoundSql对象
     * @author 李卓伦
     * @date 2025/07/25 10:48
     */
    private void processTableName(MappedStatement ms, BoundSql boundSql) {
        if (tableNameHandler == null) {
            return;
        }

        try {
            String originalSql = boundSql.getSql();
            if (originalSql == null || originalSql.trim().isEmpty()) {
                return;
            }

            // 这里可以实现更复杂的SQL解析逻辑
            // 目前使用简单的字符串替换方式
            String processedSql = processSqlTableNames(originalSql);

            if (!originalSql.equals(processedSql)) {
                log.debug("SQL表名替换完成: \n原始SQL: {}\n处理后SQL: {}", originalSql, processedSql);
                // 通过反射修改BoundSql中的SQL
                updateBoundSql(boundSql, processedSql);
            }
        } catch (Exception e) {
            log.error("处理动态表名时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理SQL中的表名
     *
     * @param sql 原始SQL
     * @return 处理后的SQL
     * @author 李卓伦
     * @date 2025/07/25 10:49
     */
    private String processSqlTableNames(String sql) {
        if (sql == null || tableNameHandler == null) {
            return sql;
        }

        // 获取当前线程的所有表名映射
        java.util.Map<String, String> tableMap = DynamicTableContextHolder.getAll();
        if (tableMap.isEmpty()) {
            return sql;
        }

        String processedSql = sql;

        try {
            // 遍历所有映射的表名进行替换
            for (java.util.Map.Entry<String, String> entry : tableMap.entrySet()) {
                String logicTableName = entry.getKey();
                String actualTableName = entry.getValue();

                if (actualTableName != null && !actualTableName.equals(logicTableName)) {
                    // 使用正则表达式进行精确的表名替换
                    // 匹配表名，确保不会误替换包含表名的其他词汇
                    String regex = "\\b" + logicTableName + "\\b";
                    processedSql = processedSql.replaceAll(regex, actualTableName);
                    log.debug("SQL表名替换: {} -> {}", logicTableName, actualTableName);
                }
            }
        } catch (Exception e) {
            log.error("处理SQL表名替换时发生异常: sql={}, error={}", sql, e.getMessage(), e);
            // 发生异常时返回原始SQL
            return sql;
        }

        return processedSql;
    }

    /**
     * 更新BoundSql中的SQL语句
     *
     * @param boundSql BoundSql对象
     * @param newSql   新的SQL语句
     * @author 李卓伦
     * @date 2025/07/25 10:50
     */
    private void updateBoundSql(BoundSql boundSql, String newSql) {
        try {
            // 通过反射修改BoundSql中的sql字段
            java.lang.reflect.Field sqlField = BoundSql.class.getDeclaredField("sql");
            sqlField.setAccessible(true);
            sqlField.set(boundSql, newSql);
        } catch (Exception e) {
            log.error("更新BoundSql失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 设置表名处理器
     *
     * @param tableNameHandler 表名处理器
     * @author 李卓伦
     * @date 2025/07/25 10:51
     */
    public void setTableNameHandler(BiFunction<String, String, String> tableNameHandler) {
        this.tableNameHandler = tableNameHandler;
        log.info("设置自定义表名处理器");
    }

    /**
     * 获取表名处理器
     *
     * @return 表名处理器
     * @author 李卓伦
     * @date 2025/07/25 10:52
     */
    public BiFunction<String, String, String> getTableNameHandler() {
        return tableNameHandler;
    }
}