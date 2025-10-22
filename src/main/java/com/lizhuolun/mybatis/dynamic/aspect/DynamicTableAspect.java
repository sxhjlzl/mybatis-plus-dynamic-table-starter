package com.lizhuolun.mybatis.dynamic.aspect;

import com.lizhuolun.mybatis.dynamic.annotation.DateSharding;
import com.lizhuolun.mybatis.dynamic.annotation.DynamicTable;
import com.lizhuolun.mybatis.dynamic.annotation.HashSharding;
import com.lizhuolun.mybatis.dynamic.context.DynamicTableContextHolder;
import com.lizhuolun.mybatis.dynamic.strategy.TableRouterStrategy;
import com.lizhuolun.mybatis.dynamic.strategy.TableRouterStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 动态表名AOP切面
 * 自动处理动态表名映射
 *
 * @author 李卓伦
 * @date 2025/01/25 10:05
 */
@Slf4j
@Aspect
@Component
@Order(1)
public class DynamicTableAspect {

    /**
     * 处理@DynamicTable注解
     *
     * @param joinPoint    连接点
     * @param dynamicTable 动态表注解
     * @return 方法执行结果
     * @throws Throwable 异常
     * @author 李卓伦
     * @date 2025/01/25 10:06
     */
    @Around("@annotation(dynamicTable)")
    public Object handleDynamicTable(ProceedingJoinPoint joinPoint, DynamicTable dynamicTable) throws Throwable {
        String logicTable = dynamicTable.value();
        Object shardingKey = extractShardingKey(joinPoint, dynamicTable);

        if (shardingKey == null) {
            log.warn("未找到分表键，使用原表名: {}", logicTable);
            return joinPoint.proceed();
        }

        return executeWithShardingKey(joinPoint, logicTable, shardingKey);
    }

    /**
     * 处理@DateSharding注解
     *
     * @param joinPoint    连接点
     * @param dateSharding 日期分表注解
     * @return 方法执行结果
     * @throws Throwable 异常
     * @author 李卓伦
     * @date 2025/01/25 10:07
     */
    @Around("@annotation(dateSharding)")
    public Object handleDateSharding(ProceedingJoinPoint joinPoint, DateSharding dateSharding) throws Throwable {
        String logicTable = dateSharding.value();
        Object dateKey = extractDateKey(joinPoint, dateSharding);

        if (dateKey == null) {
            log.warn("未找到日期键，使用原表名: {}", logicTable);
            return joinPoint.proceed();
        }

        return executeWithShardingKey(joinPoint, logicTable, dateKey);
    }

    /**
     * 处理@HashSharding注解
     *
     * @param joinPoint    连接点
     * @param hashSharding 哈希分表注解
     * @return 方法执行结果
     * @throws Throwable 异常
     * @author 李卓伦
     * @date 2025/01/25 10:08
     */
    @Around("@annotation(hashSharding)")
    public Object handleHashSharding(ProceedingJoinPoint joinPoint, HashSharding hashSharding) throws Throwable {
        String logicTable = hashSharding.value();
        Object hashKey = extractHashKey(joinPoint, hashSharding);

        if (hashKey == null) {
            log.warn("未找到哈希键，使用原表名: {}", logicTable);
            return joinPoint.proceed();
        }

        return executeWithShardingKey(joinPoint, logicTable, hashKey);
    }

    /**
     * 提取分表键
     *
     * @param joinPoint    连接点
     * @param dynamicTable 动态表注解
     * @return 分表键
     * @author 李卓伦
     * @date 2025/01/25 10:09
     */
    private Object extractShardingKey(ProceedingJoinPoint joinPoint, DynamicTable dynamicTable) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        // 优先使用参数名
        if (!dynamicTable.shardingKey().isEmpty()) {
            return extractByParamName(method, args, dynamicTable.shardingKey());
        }

        // 使用参数索引
        int index = dynamicTable.shardingKeyIndex();
        if (index >= 0 && index < args.length) {
            return args[index];
        }

        return null;
    }

    /**
     * 提取日期键
     *
     * @param joinPoint    连接点
     * @param dateSharding 日期分表注解
     * @return 日期键
     * @author 李卓伦
     * @date 2025/01/25 10:10
     */
    private Object extractDateKey(ProceedingJoinPoint joinPoint, DateSharding dateSharding) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        // 优先使用参数名
        if (!dateSharding.dateParam().isEmpty()) {
            return extractByParamName(method, args, dateSharding.dateParam());
        }

        // 使用参数索引
        int index = dateSharding.dateIndex();
        if (index >= 0 && index < args.length) {
            return args[index];
        }

        return null;
    }

    /**
     * 提取哈希键
     *
     * @param joinPoint    连接点
     * @param hashSharding 哈希分表注解
     * @return 哈希键
     * @author 李卓伦
     * @date 2025/01/25 10:11
     */
    private Object extractHashKey(ProceedingJoinPoint joinPoint, HashSharding hashSharding) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        // 优先使用参数名
        if (!hashSharding.hashKey().isEmpty()) {
            return extractByParamName(method, args, hashSharding.hashKey());
        }

        // 使用参数索引
        int index = hashSharding.hashKeyIndex();
        if (index >= 0 && index < args.length) {
            return args[index];
        }

        return null;
    }

    /**
     * 根据参数名提取参数值
     *
     * @param method    方法
     * @param args      参数数组
     * @param paramName 参数名
     * @return 参数值
     * @author 李卓伦
     * @date 2025/01/25 10:12
     */
    private Object extractByParamName(Method method, Object[] args, String paramName) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (paramName.equals(parameters[i].getName())) {
                return args[i];
            }
        }
        return null;
    }

    /**
     * 使用分表键执行方法
     *
     * @param joinPoint   连接点
     * @param logicTable  逻辑表名
     * @param shardingKey 分表键
     * @return 方法执行结果
     * @throws Throwable 异常
     * @author 李卓伦
     * @date 2025/01/25 10:13
     */
    private Object executeWithShardingKey(ProceedingJoinPoint joinPoint, String logicTable, Object shardingKey) throws Throwable {
        TableRouterStrategy strategy = TableRouterStrategyFactory.getStrategy(logicTable);
        if (strategy == null) {
            log.warn("未找到匹配的分表策略: {}", logicTable);
            return joinPoint.proceed();
        }

        String actualTable = strategy.getActualTableName(logicTable, shardingKey);
        if (actualTable == null || actualTable.equals(logicTable)) {
            log.debug("表名未发生变化: {}", logicTable);
            return joinPoint.proceed();
        }

        try {
            DynamicTableContextHolder.set(logicTable, actualTable);
            log.debug("设置表名映射: {} -> {}", logicTable, actualTable);
            return joinPoint.proceed();
        } catch (Exception e) {
            log.error("执行动态表名操作时发生异常: logicTable={}, shardingKey={}, error={}",
                    logicTable, shardingKey, e.getMessage(), e);
            throw e;
        } finally {
            DynamicTableContextHolder.remove(logicTable);
            log.debug("清理表名映射: {}", logicTable);
        }
    }
}
