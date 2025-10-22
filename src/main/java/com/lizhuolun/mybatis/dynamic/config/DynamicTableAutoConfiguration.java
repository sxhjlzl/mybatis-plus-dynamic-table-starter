package com.lizhuolun.mybatis.dynamic.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.lizhuolun.mybatis.dynamic.aspect.DynamicTableAspect;
import com.lizhuolun.mybatis.dynamic.interceptor.DynamicTableNameInnerInterceptor;
import com.lizhuolun.mybatis.dynamic.strategy.TableRouterStrategyFactory;
import com.lizhuolun.mybatis.dynamic.strategy.impl.DateBasedTableRouterStrategy;
import com.lizhuolun.mybatis.dynamic.strategy.impl.HashBasedTableRouterStrategy;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.List;

/**
 * 动态表名自动配置类
 * 自动配置MyBatis-Plus动态表名相关组件
 *
 * @author 李卓伦
 * @date 2025/07/25 11:00
 */
@Slf4j
@Configuration
@ConditionalOnClass({MybatisPlusInterceptor.class})
@ConditionalOnProperty(prefix = "dynamic-table", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DynamicTableProperties.class)
public class DynamicTableAutoConfiguration {

    /**
     * 动态表名配置属性
     **/
    private final DynamicTableProperties properties;

    /**
     * 构造函数
     *
     * @param properties 配置属性
     * @author 李卓伦
     * @date 2025/07/25 11:01
     */
    public DynamicTableAutoConfiguration(DynamicTableProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("DynamicTableProperties不能为空");
        }
        this.properties = properties;
    }

    /**
     * 自动配置初始化
     *
     * @author 李卓伦
     * @date 2025/07/25 11:02
     */
    @PostConstruct
    public void init() {
        log.info("动态表名自动配置开始初始化");

        try {
            // 注册日期分表策略
            registerDateShardingStrategies();

            // 注册哈希分表策略
            registerHashShardingStrategies();

            // 注册新格式的表配置策略
            registerTableConfigStrategies();

            log.info("动态表名自动配置初始化完成，已注册{}个策略",
                    TableRouterStrategyFactory.getStrategyCount());
            log.info(TableRouterStrategyFactory.getStrategyInfo());
        } catch (Exception e) {
            log.error("动态表名自动配置初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("动态表名自动配置初始化失败", e);
        }
    }

    /**
     * 配置MyBatis-Plus拦截器
     *
     * @return MybatisPlusInterceptor 实例
     * @author 李卓伦
     * @date 2025/07/25 11:03
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加动态表名拦截器
        DynamicTableNameInnerInterceptor dynamicTableInterceptor = new DynamicTableNameInnerInterceptor();
        interceptor.addInnerInterceptor(dynamicTableInterceptor);

        log.info("MyBatis-Plus动态表名拦截器配置完成");
        return interceptor;
    }

    /**
     * 动态表名内部拦截器Bean
     *
     * @return DynamicTableNameInnerInterceptor 实例
     * @author 李卓伦
     * @date 2025/07/25 11:04
     */
    @Bean
    @ConditionalOnMissingBean
    public DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor() {
        DynamicTableNameInnerInterceptor interceptor = new DynamicTableNameInnerInterceptor();
        log.info("创建动态表名内部拦截器Bean");
        return interceptor;
    }

    /**
     * 动态表名AOP切面Bean
     *
     * @return DynamicTableAspect 实例
     * @author 李卓伦
     * @date 2025/01/25 11:05
     */
    @Bean
    @ConditionalOnMissingBean
    public DynamicTableAspect dynamicTableAspect() {
        DynamicTableAspect aspect = new DynamicTableAspect();
        log.info("创建动态表名AOP切面Bean");
        return aspect;
    }

    /**
     * 注册日期分表策略
     *
     * @author 李卓伦
     * @date 2025/07/25 11:05
     */
    private void registerDateShardingStrategies() {
        List<DynamicTableProperties.DateShardingConfig> dateConfigs = properties.getDateSharding();
        if (dateConfigs == null || dateConfigs.isEmpty()) {
            log.debug("未配置日期分表策略");
            return;
        }

        int registeredCount = 0;
        for (DynamicTableProperties.DateShardingConfig config : dateConfigs) {
            if (!isValidDateConfig(config)) {
                continue;
            }

            DateBasedTableRouterStrategy strategy = new DateBasedTableRouterStrategy(
                    new HashSet<>(config.getTables()),
                    config.getDatePattern(),
                    config.getPriority()
            );

            TableRouterStrategyFactory.register(strategy);
            registeredCount++;
            log.debug("注册日期分表策略: 表={}, 日期格式={}, 优先级={}",
                    config.getTables(), config.getDatePattern(), config.getPriority());
        }

        log.info("日期分表策略注册完成，共注册{}个策略", registeredCount);
    }

    /**
     * 注册哈希分表策略
     *
     * @author 李卓伦
     * @date 2025/07/25 11:06
     */
    private void registerHashShardingStrategies() {
        List<DynamicTableProperties.HashShardingConfig> hashConfigs = properties.getHashSharding();
        if (hashConfigs == null || hashConfigs.isEmpty()) {
            log.debug("未配置哈希分表策略");
            return;
        }

        int registeredCount = 0;
        for (DynamicTableProperties.HashShardingConfig config : hashConfigs) {
            if (!isValidHashConfig(config)) {
                continue;
            }

            HashBasedTableRouterStrategy strategy = new HashBasedTableRouterStrategy(
                    new HashSet<>(config.getTables()),
                    config.getTableCount(),
                    config.getPriority()
            );

            TableRouterStrategyFactory.register(strategy);
            registeredCount++;
            log.debug("注册哈希分表策略: 表={}, 分表数量={}, 优先级={}",
                    config.getTables(), config.getTableCount(), config.getPriority());
        }

        log.info("哈希分表策略注册完成，共注册{}个策略", registeredCount);
    }

    /**
     * 注册新格式的表配置策略
     *
     * @author 李卓伦
     * @date 2025/07/25 15:50
     */
    private void registerTableConfigStrategies() {
        List<DynamicTableProperties.TableConfig> tableConfigs = properties.getTables();
        if (tableConfigs == null || tableConfigs.isEmpty()) {
            log.debug("未配置新格式的表策略");
            return;
        }

        for (DynamicTableProperties.TableConfig config : tableConfigs) {
            if (config.getTableName() == null || config.getTableName().trim().isEmpty()) {
                log.warn("表配置中表名为空，跳过该配置");
                continue;
            }

            if (config.getStrategy() == null || config.getStrategy().trim().isEmpty()) {
                log.warn("表配置中策略为空，跳过该配置: {}", config.getTableName());
                continue;
            }

            HashSet<String> tableSet = new HashSet<>();
            tableSet.add(config.getTableName());

            String strategy = config.getStrategy().toUpperCase();
            switch (strategy) {
                case "DATE":
                    DateBasedTableRouterStrategy dateStrategy = new DateBasedTableRouterStrategy(
                            tableSet,
                            config.getFormat() != null ? config.getFormat() : "yyyyMM",
                            config.getPriority() != null ? config.getPriority() : 50
                    );
                    TableRouterStrategyFactory.register(dateStrategy);
                    log.info("注册日期分表策略: 表={}, 日期格式={}, 优先级={}",
                            config.getTableName(), config.getFormat(), config.getPriority());
                    break;

                case "HASH":
                    HashBasedTableRouterStrategy hashStrategy = new HashBasedTableRouterStrategy(
                            tableSet,
                            config.getModValue() != null ? config.getModValue() : 10,
                            config.getPriority() != null ? config.getPriority() : 60
                    );
                    TableRouterStrategyFactory.register(hashStrategy);
                    log.info("注册哈希分表策略: 表={}, 取模值={}, 优先级={}",
                            config.getTableName(), config.getModValue(), config.getPriority());
                    break;

                default:
                    log.warn("不支持的分表策略: {}, 表名: {}", strategy, config.getTableName());
                    break;
            }
        }
    }

    /**
     * 验证日期分表配置有效性
     *
     * @param config 日期分表配置
     * @return 是否有效
     * @author 李卓伦
     * @date 2025/07/25 11:30
     */
    private boolean isValidDateConfig(DynamicTableProperties.DateShardingConfig config) {
        if (config == null) {
            log.warn("日期分表配置为空");
            return false;
        }

        if (config.getTables() == null || config.getTables().isEmpty()) {
            log.warn("日期分表配置中表名列表为空，跳过该配置");
            return false;
        }

        if (config.getDatePattern() == null || config.getDatePattern().trim().isEmpty()) {
            log.warn("日期分表配置的日期格式为空，跳过该配置");
            return false;
        }

        return true;
    }

    /**
     * 验证哈希分表配置有效性
     *
     * @param config 哈希分表配置
     * @return 是否有效
     * @author 李卓伦
     * @date 2025/07/25 11:31
     */
    private boolean isValidHashConfig(DynamicTableProperties.HashShardingConfig config) {
        if (config == null) {
            log.warn("哈希分表配置为空");
            return false;
        }

        if (config.getTables() == null || config.getTables().isEmpty()) {
            log.warn("哈希分表配置中表名列表为空，跳过该配置");
            return false;
        }

        if (config.getTableCount() <= 0) {
            log.warn("哈希分表配置的表数量无效: {}，跳过该配置", config.getTableCount());
            return false;
        }

        return true;
    }
}