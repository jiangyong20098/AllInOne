package com.huawei.allinone.thread.threadlocal;

import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池中线程上下文丢失
 * <p>
 * ThreadLocal 不能在父子线程中传递，因此最常见的做法是把父线程中的 ThreadLocal 值拷贝到子线程中。
 * <p>
 * 这么写倒也没有问题，我们再看看线程池的设置：
 * ThreadPoolExecutor executorPool = new ThreadPoolExecutor(2, 4, 3, TimeUnit.SECONDS,
 * new LinkedBlockingQueue<Runnable>(4), new XXXThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
 * 其中最后一个参数控制着当线程池满时，该如何处理提交的任务，内置有 4 种策略：
 * ThreadPoolExecutor.AbortPolicy //直接抛出异常
 * ThreadPoolExecutor.DiscardPolicy //丢弃当前任务
 * ThreadPoolExecutor.DiscardOldestPolicy //丢弃工作队列头部的任务
 * ThreadPoolExecutor.CallerRunsPolicy //转串行执行
 * 可以看到，我们初始化线程池的时候指定如果线程池满，则新提交的任务转为串行执行。
 * <p>
 * 那我们之前的写法就会有问题了，串行执行的时候调用 ContextHolder.remove(); 会将主线程的上下文也清理，即使后面线程池继续并行工作，传给子线程的上下文也已经是 null 了，而且这样的问题很难在预发测试的时候发现。
 */
@Slf4j
public class ThreadPoolContextLose {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4, 3, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(4), new DefaultThreadFactory("jy-pool"), new ThreadPoolExecutor.CallerRunsPolicy());

        List<Future<?>> results = new ArrayList<>();
        ContextHolder.set("xxx000");
        for (int i = 0; i < 20; i++) {
            // 提交任务，并设置拷贝Context到子线程
            Future<?> taskResult = threadPoolExecutor.submit(new BizTask(ContextHolder.get()));
            results.add(taskResult);
        }

        for (Future<?> result : results) {
            //阻塞等待任务执行完成
            result.get();
        }

        threadPoolExecutor.shutdown();
    }
}

@Slf4j
class BizTask<T> implements Callable<T> {
    private Random random = new Random();
    private String session = null;

    public BizTask(String session) {
        this.session = session;
    }

    @Override
    public T call() {
        try {
            ContextHolder.set(this.session);

            // 执行业务逻辑
            int anInt = random.nextInt(10);
            log.info("this.session: {}, 耗时：{}", this.session, anInt);
            TimeUnit.SECONDS.sleep(anInt);
        } catch (Exception e) {
            log.error("error: {}", e);
        } finally {
            // 清理 ThreadLocal 的上下文，避免线程复用时context互串
            ContextHolder.remove();
        }
        return null;
    }
}

@Slf4j
class ContextHolder {
    private static ThreadLocal<String> localThreadCache = new ThreadLocal<>();

    public static void set(String cacheValue) {
        localThreadCache.set(cacheValue);
    }

    public static String get() {
        return localThreadCache.get();
    }

    public static void remove() {
        log.info("execute remove()");
        localThreadCache.remove();
    }
}

/**
 * this.session: null即线程上下文丢失了
 * 00:29:09.743 [main] DEBUG io.netty.util.internal.logging.InternalLoggerFactory - Using SLF4J as the default logging framework
 * 00:29:09.753 [jy-pool-1-4] INFO BizTask - this.session: xxx000, 耗时：0
 * 00:29:09.753 [jy-pool-1-4] INFO ContextHolder - execute remove()
 * 00:29:09.753 [main] INFO BizTask - this.session: xxx000, 耗时：1
 * 00:29:09.753 [jy-pool-1-1] INFO BizTask - this.session: xxx000, 耗时：5
 * 00:29:09.753 [jy-pool-1-3] INFO BizTask - this.session: xxx000, 耗时：9
 * 00:29:09.753 [jy-pool-1-2] INFO BizTask - this.session: xxx000, 耗时：3
 * 00:29:09.753 [jy-pool-1-4] INFO BizTask - this.session: xxx000, 耗时：9
 * 00:29:10.754 [main] INFO ContextHolder - execute remove()
 * 00:29:10.754 [main] INFO BizTask - this.session: null, 耗时：8
 * 00:29:12.754 [jy-pool-1-2] INFO ContextHolder - execute remove()
 * 00:29:12.754 [jy-pool-1-2] INFO BizTask - this.session: xxx000, 耗时：4
 * 00:29:14.754 [jy-pool-1-1] INFO ContextHolder - execute remove()
 * 00:29:14.754 [jy-pool-1-1] INFO BizTask - this.session: xxx000, 耗时：5
 * 00:29:16.755 [jy-pool-1-2] INFO ContextHolder - execute remove()
 * 00:29:16.755 [jy-pool-1-2] INFO BizTask - this.session: xxx000, 耗时：2
 * 00:29:18.755 [main] INFO ContextHolder - execute remove()
 * 00:29:18.755 [jy-pool-1-2] INFO ContextHolder - execute remove()
 * 00:29:18.755 [jy-pool-1-2] INFO BizTask - this.session: null, 耗时：1
 * 00:29:18.755 [main] INFO BizTask - this.session: null, 耗时：6
 * 00:29:18.755 [jy-pool-1-4] INFO ContextHolder - execute remove()
 * 00:29:18.755 [jy-pool-1-4] INFO BizTask - this.session: null, 耗时：2
 * 00:29:18.755 [jy-pool-1-3] INFO ContextHolder - execute remove()
 * 00:29:18.755 [jy-pool-1-3] INFO BizTask - this.session: null, 耗时：0
 * 00:29:18.765 [jy-pool-1-3] INFO ContextHolder - execute remove()
 * 00:29:18.765 [jy-pool-1-3] INFO BizTask - this.session: null, 耗时：3
 * 00:29:19.756 [jy-pool-1-2] INFO ContextHolder - execute remove()
 * 00:29:19.756 [jy-pool-1-1] INFO ContextHolder - execute remove()
 * 00:29:19.756 [jy-pool-1-2] INFO BizTask - this.session: null, 耗时：1
 * 00:29:20.756 [jy-pool-1-4] INFO ContextHolder - execute remove()
 * 00:29:20.766 [jy-pool-1-2] INFO ContextHolder - execute remove()
 * 00:29:21.766 [jy-pool-1-3] INFO ContextHolder - execute remove()
 * 00:29:22.766 [jy-pool-1-1] DEBUG io.netty.util.internal.InternalThreadLocalMap - -Dio.netty.threadLocalMap.stringBuilder.initialSize: 1024
 * 00:29:22.766 [jy-pool-1-1] DEBUG io.netty.util.internal.InternalThreadLocalMap - -Dio.netty.threadLocalMap.stringBuilder.maxSize: 4096
 * 00:29:24.756 [main] INFO ContextHolder - execute remove()
 * 00:29:24.756 [jy-pool-1-3] INFO BizTask - this.session: null, 耗时：0
 * 00:29:24.756 [jy-pool-1-2] INFO BizTask - this.session: null, 耗时：4
 * 00:29:24.756 [jy-pool-1-3] INFO ContextHolder - execute remove()
 * 00:29:24.756 [jy-pool-1-3] INFO BizTask - this.session: null, 耗时：3
 * 00:29:27.757 [jy-pool-1-3] INFO ContextHolder - execute remove()
 * 00:29:27.757 [jy-pool-1-3] INFO BizTask - this.session: null, 耗时：1
 * 00:29:28.757 [jy-pool-1-2] INFO ContextHolder - execute remove()
 * 00:29:28.757 [jy-pool-1-3] INFO ContextHolder - execute remove()
 *
 * Process finished with exit code 0
 */

