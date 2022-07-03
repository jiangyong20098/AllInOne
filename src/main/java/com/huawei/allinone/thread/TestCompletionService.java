package com.huawei.allinone.thread;

import ch.qos.logback.core.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TestCompletionService {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        TestCompletionService.testCompletionServiceBad();
        TestCompletionService.testCompletionService0();
        TestCompletionService.testExecutorService();
        TestCompletionService.testCompletionService();
        TestCompletionService.executorCompletionService();
        log.info("end");
    }

    /**
     * CompletionService结果没调用take、poll方法，会引起OOM
     * 因为 OOM 是一个内存缓慢增长的过程，稍微粗心大意就会忽略。如果是这个代码块的调用量少的话，很可能几天甚至几个月后暴雷。
     * <p>
     * 只有调用了ExecutorCompletionService的take或poll方法时，阻塞队列中的task执行结果才会从队列中移除掉，释放堆内存。
     * 由于该业务不需要使用任务的返回值，没有调用take、poll方法，从而导致没有释放堆内存。堆内存会随着调用量的增加一直增长。
     * 所以，业务场景中不需要使用任务返回值的，别没事儿使用CompletionService。假如使用了，记得一定要从阻塞队列中移除掉task执行结果，避免OOM！
     */
    public static void testCompletionServiceBad() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CompletionService<String> completionService = new ExecutorCompletionService<>(executorService);
        Future<String> future = completionService.submit(() -> "Hello_" + Thread.currentThread().getName());

        // 加了这句cancel好像也没有什么用
        future.cancel(true);

        // 一定要关闭线程池，否则会引起OOM
        executorService.shutdown();
    }

    /**
     * ExecutorService阻塞等待
     * <p>
     * 15:05:10.358 [main]  - 通知大家聚餐，司机开车接人
     * 15:05:10.412 [pool-1-thread-1]  - 总裁：我要上大号，要蹲1小时
     * 15:05:10.413 [pool-1-thread-2]  - 销售经理：我要上大号，要蹲3分钟
     * 15:05:10.413 [pool-1-thread-3]  - 小秘：我要上大号，要蹲40分钟
     * 15:05:10.415 [main]  - 通知完毕，等着接吧。
     * 15:05:10.417 [pool-1-thread-2]  - 销售经理：终于完事，你来接吧！   // 销售经理最先完事
     * 15:05:10.420 [pool-1-thread-3]  - 小秘：终于完事，你来接吧！      // 小秘其次完事
     * 15:05:10.424 [pool-1-thread-1]  - 总裁：终于完事，你来接吧！      // 总裁最后完事
     * 15:05:10.424 [main]  - 总裁上完大号了，你去接他       // 虽然总裁最后完事，但是也要等他
     * 15:05:10.426 [main]  - 销售经理上完大号了，你去接他   // 其实就是加入等待队列的顺序，因为future.get()操作，阻塞等待
     * 15:05:10.426 [main]  - 小秘上完大号了，你去接他       // 小秘最后通知的，所以最后接她
     * 15:05:10.426 [main]  - end
     * <p>
     * Process finished with exit code 0
     *
     * @throws InterruptedException
     */
    public static void testExecutorService() throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Future<String>> futureList = new ArrayList<>();
        log.info("通知大家聚餐，司机开车接人");
        Future<String> future1 = executorService.submit(() -> {
            log.info("总裁：我要上大号，要蹲1小时");
            Thread.sleep(10);
            log.info("总裁：终于完事，你来接吧！");
            return "总裁上完大号了";
        });
        futureList.add(future1);

        Future<String> future2 = executorService.submit(() -> {
            log.info("销售经理：我要上大号，要蹲3分钟");
            Thread.sleep(3);
            log.info("销售经理：终于完事，你来接吧！");
            return "销售经理上完大号了";
        });
        futureList.add(future2);

        Future<String> future3 = executorService.submit(() -> {
            log.info("小秘：我要上大号，要蹲40分钟");
            Thread.sleep(6);
            log.info("小秘：终于完事，你来接吧！");
            return "小秘上完大号了";
        });
        futureList.add(future3);

        Thread.sleep(1);
        log.info("通知完毕，等着接吧。");

        try {
            for (Future<String> future : futureList) {
                // 阻塞等待
                String rtn = future.get();
                log.info("{}，你去接他", rtn);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        // 一定要关闭线程池，否则会引起OOM
        executorService.shutdown();
    }

    /**
     * CompletionService异步，谁先完成就先接谁
     * <p>
     * 15:26:43.865 [main]  - 通知大家聚餐，司机开车接人
     * 15:26:43.913 [pool-1-thread-1]  - 总裁：我要上大号，要蹲1小时
     * 15:26:43.913 [pool-1-thread-2]  - 销售经理：我要上大号，要蹲3分钟
     * 15:26:43.913 [pool-1-thread-3]  - 小秘：我要上大号，要蹲40分钟
     * 15:26:43.915 [main]  - 通知完毕，等着接吧。
     * 15:26:43.917 [pool-1-thread-2]  - 销售经理：终于完事，你来接吧！
     * 15:26:43.917 [main]  - 销售经理上完大号了，你去接他
     * 15:26:43.920 [pool-1-thread-3]  - 小秘：终于完事，你来接吧！
     * 15:26:43.920 [main]  - 小秘上完大号了，你去接他
     * 15:26:43.924 [pool-1-thread-1]  - 总裁：终于完事，你来接吧！
     * 15:26:43.924 [main]  - 总裁上完大号了，你去接他
     * 15:26:43.924 [main]  - 主綫程阻塞等待
     *
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static void testCompletionService() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        CompletionService<String> completionService = new ExecutorCompletionService<>(executorService);
        List<Future<String>> futureList = new ArrayList<>();

        log.info("通知大家聚餐，司机开车接人");
        Future<String> future1 = completionService.submit(() -> {
            log.info("总裁：我要上大号，要蹲1小时");
            Thread.sleep(10);
            log.info("总裁：终于完事，你来接吧！");
            return "总裁上完大号了";
        });
        futureList.add(future1);

        Future<String> future2 = completionService.submit(() -> {
            log.info("销售经理：我要上大号，要蹲3分钟");
            Thread.sleep(3);
            log.info("销售经理：终于完事，你来接吧！");
            return "销售经理上完大号了";
        });
        futureList.add(future2);

        Future<String> future3 = completionService.submit(() -> {
            log.info("小秘：我要上大号，要蹲40分钟");
            Thread.sleep(6);
            log.info("小秘：终于完事，你来接吧！");
            return "小秘上完大号了";
        });
        futureList.add(future3);

        Thread.sleep(1);
        log.info("通知完毕，等着接吧。");

        for (int i = 0; i < futureList.size(); i++) {
            String rtn = completionService.take().get();
            log.info("{}，你去接他", rtn);
        }

        for (Future<String> future : futureList) {
            future.cancel(true);
        }

        // 一定要关闭线程池，否则会引起OOM
        executorService.shutdown();

        log.info("主綫程阻塞等待");
        Thread.currentThread().join(3000);
    }

    /**
     * CompletionService正确用法
     * 调用take()、poll()等
     * <p>
     * 我们在使用ExecutorService submit提交任务后需要关注每个任务返回的future。
     * 然而CompletionService对这些future进行了追踪，并且重写了done方法，让你等的completionQueue队列里面一定是完成了的task；
     *
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static void testCompletionService0() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        /**
         * ExecutorCompletionService入参需要传一个线程池对象，默认使用的队列是LinkedBlockingQueue，
         * 不过还有另外一个构造方法可以指定队列类型，ExecutorCompletionService(Executor executor, BlockingQueue<Future<V>> completionQueue)
         * submit方法里有executor.execute(new QueueingFuture(f));
         * QueueingFuture继承自FutureTask，重写了 done() 方法；         *
         * 把task放到completionQueue队列里面。当任务执行完成后，task就会被放到队列里面去了；         *
         * 此时此刻，completionQueue队列里面的task都是已经done()完成了的task。而这个task就是我们拿到的一个个的future结果；         *
         * 如果调用completionQueue的take方法，会阻塞等待任务。等到的一定是完成了的future，我们调用.get()方法 就能立马获得结果。
         */
        CompletionService<String> completionService = new ExecutorCompletionService<>(executorService);

        // 有返回值的要调用CompletionService的take()、get()从完成队列取结果
        //completionService.submit(() -> "Hello_" + Thread.currentThread().getName());
        completionService.submit(() -> "World_" + Thread.currentThread().getName());
        for (int i = 0; i < 1; i++) {
            /**
             * poll与take方法不同, poll有两个版本:
             * 无参的poll方法 --- 如果完成队列中有数据就返回, 否则返回null
             * 有参数的poll方法 --- 如果完成队列中有数据就直接返回, 否则等待指定的时间, 到时间后如果还是没有数据就返回null
             * ExecutorCompletionService主要用与管理异步任务 (有结果的任务, 任务完成后要处理结果)
             * take()会一直等待，直到完成队列里有元素
             */
//            Future<String> future = completionService.take();

            Future<String> future = completionService.poll(3, TimeUnit.SECONDS);
            if (Objects.nonNull(future)) {
                String rtn = future.get();
                log.info("rtn = {}", rtn);
                future.cancel(true);
            } else {
                log.info("future is null.");
            }
        }

        // 一定要关闭线程池，否则会引起OOM
        executorService.shutdown();
    }


    /**
     * <三> 使用ExecutorCompletionService管理异步任务
     * 1. Java中的ExecutorCompletionService<V>本身有管理任务队列的功能
     * i. ExecutorCompletionService内部维护列一个队列, 用于管理已完成的任务
     * ii. 内部还维护列一个Executor, 可以执行任务
     * <p>
     * 2. ExecutorCompletionService内部维护了一个BlockingQueue, 只有完成的任务才被加入到队列中
     * <p>
     * 3. 任务一完成就加入到内置管理队列中, 如果队列中的数据为空时, 调用take()就会阻塞 (等待任务完成)
     * i. 关于完成任务是如何加入到完成队列中的, 请参考ExecutorCompletionService的内部类QueueingFuture的done()方法
     * <p>
     * 4. ExecutorCompletionService的take/poll方法是对BlockingQueue对应的方法的封装, 关于BlockingQueue的take/poll方法:
     * i. take()方法, 如果队列中有数据, 就返回数据, 否则就一直阻塞;
     * ii. poll()方法: 如果有值就返回, 否则返回null
     * iii. poll(long timeout, TimeUnit unit)方法: 如果有值就返回, 否则等待指定的时间; 如果时间到了如果有值, 就返回值, 否则返回null
     * <p>
     * 解决了已完成任务得不到及时处理的问题
     */
    static void executorCompletionService() throws InterruptedException, ExecutionException {
        Random random = new Random();

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CompletionService<String> completionService = new ExecutorCompletionService<>(executorService);

        for (int i = 0; i < 50; i++) {
            completionService.submit(() -> {
                Thread.sleep(random.nextInt(5000));
                return Thread.currentThread().getName();
            });
        }

        int completionTask = 0;
        while (completionTask < 50) {
            // 如果完成队列中没有数据, 则阻塞; 否则返回队列中的数据
            Future<String> resultHolder = completionService.take();
            System.out.println("result: " + resultHolder.get());
            completionTask++;
        }

        System.out.println(completionTask + " task done !");

        // ExecutorService使用完一定要关闭 (回收资源, 否则系统资源耗尽! .... 呵呵...)
        executorService.shutdown();
    }
}
