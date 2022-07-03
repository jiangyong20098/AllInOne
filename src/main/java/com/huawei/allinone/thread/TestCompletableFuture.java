package com.huawei.allinone.thread;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * CompletableFuture.allOf使用
 */
@Slf4j
public class TestCompletableFuture {
    /**
     * 随机数
     */
    private static Random random = new Random();

    /**
     * 使用自己的线程池
     */
    private final static ThreadPoolExecutor executorServe = new ThreadPoolExecutor(30,
            Runtime.getRuntime().availableProcessors() * 30,
            5,
            TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(1000));

    /**
     * 入口
     *
     * @param args 参数
     */
    public static void main(String[] args) {
        log.info("cpu:{} ", Runtime.getRuntime().availableProcessors());

        // 开始计时
        StopWatch stopWatch = StopWatch.createStarted();

        List<CompletableFuture<String>> list = new ArrayList<>();

        int taskNum = random.nextInt(50) + 1;
        for (int i = 0; i < taskNum; i++) {
            list.add(CompletableFuture.supplyAsync(() -> queryUserInfos(), executorServe));
        }

        CompletableFuture.allOf(list.toArray(new CompletableFuture[list.size()])).join();

        log.info(String.format(Locale.ENGLISH, "********所有任务（%d个）全部执行完毕，总耗时:%d秒 ********", taskNum, stopWatch.getTime(TimeUnit.SECONDS)));

        // 打印结果
        list.stream().forEach((cf) -> {
            log.info(cf.join());
        });

        executorServe.shutdown();
        log.info("bye bye.");
    }

    /**
     * 模拟耗时操作
     */
    private static String queryUserInfos() {
        int anInt = random.nextInt(10);
        String info = String.format(Locale.ENGLISH, "----当前线程%s,随机数：%d秒----", Thread.currentThread().getName(), anInt);
        log.info(info);
        try {
            TimeUnit.SECONDS.sleep(anInt);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "【结束返回】" + info;
    }
}