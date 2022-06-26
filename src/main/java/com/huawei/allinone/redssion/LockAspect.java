package com.huawei.allinone.redssion;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁切面
 */
@Slf4j
@Aspect
@Order(1000)
@Component
public class LockAspect {
    public static final String TASK_PUSH_LOCK_PREFIX = "cms:";

    @Autowired
    private RedissonClient redissonClient;
    private static DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
    private static SpelExpressionParser parser = new SpelExpressionParser();

    //增强带有Lock注解的方法
    @Pointcut("@annotation(com.xxx.cms.service.annotation.Lock)")
    public void LockAspect() {
    }

    @Around("LockAspect()")
    public Object redisLock(ProceedingJoinPoint point) throws Throwable {
        Object result;
        Class<?> clazz = point.getTarget().getClass();
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = clazz.getDeclaredMethod(methodSignature.getName(), methodSignature.getParameterTypes());
        Lock redisLockAnnotation = method.getAnnotation(Lock.class);
        // 拼接 key
        final String key = generateKeyBySpEL(redisLockAnnotation.key(), method, point.getArgs());

        String prefix = StringUtils.isEmpty(redisLockAnnotation.prefix()) ? TASK_PUSH_LOCK_PREFIX + ":" + method.getName() + ":" : redisLockAnnotation.prefix();
        String lockKey = StringUtils.isEmpty(key)
                ? prefix
                : prefix + ":" + key;
        log.info("分布式锁,创建key:{}", lockKey);
        //redisson核心步骤1 getLock 获取锁
        final RLock lock = redissonClient.getLock(lockKey);

        try {
            lockKeyList.add(new RedisLockInfo(lockKey, redisLockAnnotation.expire(), redisLockAnnotation.tryCount(), Thread.currentThread()));
            //redisson核心步骤2 lock  加锁
            lock.lock(redisLockAnnotation.expire(), TimeUnit.SECONDS);
            result = point.proceed();
            //redisson核心步骤3 unlock 释放锁
            lock.unlock();
            log.info("# [END]分布式锁,删除key:{}", lockKey);
            return result;
        } catch (CommonException ie) {
            log.error("分布式锁,请求超时", ie.getMessage());
            throw new Exception(ie.getMessage());
        } catch (Exception e) {
            log.error("分布式锁,创建失败", e);
            //redisson核心步骤4 unlock 最后记得释放锁
            lock.unlock();
            log.info("# [END]分布式锁,删除key:{}", lockKey);
        }
        return false;
    }

    private static String generateKeyBySpEL(String spELString, Method method, Object[] args) {
        String[] paramNames = discoverer.getParameterNames(method);
        Expression expression = parser.parseExpression(spELString);
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < args.length; i++) {
            assert paramNames != null;
            context.setVariable(paramNames[i], args[i]);
        }
        return Objects.requireNonNull(expression.getValue(context)).toString();
    }

    @Data
    class RedisLockInfo {
        private String key;
        private int tryNums;
        private int tryCount;
        private Thread thread;
        private long expireTime;

        public RedisLockInfo(String key, long expireTime, int tryCount, Thread thread) {
            this.key = key;
            this.tryCount = tryCount;
            this.thread = thread;
            this.expireTime = expireTime;
        }
    }
}	