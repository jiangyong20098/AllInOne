package com.huawei.allinone.thread.threadlocal;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

public class ParallelStreamContextLose {
    public static void main(String[] args) {
        List<Integer> dataList = Arrays.asList(1, 5, 2, 5, 3, 7, 3, 7, 4, 8, 9, 1, 5, 2, 5, 3, 7, 3, 7, 4, 8, 9);
        ContextHolder.set(dataList.size() + "");
        ParallelProcessor<Integer> integerParallelProcessor = new ParallelProcessor<>();
         integerParallelProcessor.process(dataList);

        ParallelProcessor2<Integer> integerParallelProcessor2 = new ParallelProcessor2<>("xxoo");
        integerParallelProcessor2.process(dataList);
    }

    /**
     * 01:00:51.466 [main] INFO ParallelProcessor - doIt().session: 11
     * 01:00:51.466 [ForkJoinPool.commonPool-worker-3] INFO ParallelProcessor - doIt().session: null
     * 01:00:51.466 [ForkJoinPool.commonPool-worker-1] INFO ParallelProcessor - doIt().session: null
     * 01:00:51.466 [ForkJoinPool.commonPool-worker-3] INFO ParallelProcessor - doIt().session: null
     * 01:00:51.466 [ForkJoinPool.commonPool-worker-1] INFO ParallelProcessor - doIt().session: null
     * 01:00:51.466 [ForkJoinPool.commonPool-worker-2] INFO ParallelProcessor - doIt().session: null
     * 01:00:51.466 [ForkJoinPool.commonPool-worker-3] INFO ParallelProcessor - doIt().session: null
     * 01:00:51.466 [main] INFO ParallelProcessor - doIt().session: 11
     * 01:00:51.466 [ForkJoinPool.commonPool-worker-2] INFO ParallelProcessor - doIt().session: null
     * 01:00:51.466 [ForkJoinPool.commonPool-worker-3] INFO ParallelProcessor - doIt().session: null
     * 01:00:51.466 [ForkJoinPool.commonPool-worker-1] INFO ParallelProcessor - doIt().session: null
     */
    @Slf4j
    static class ParallelProcessor<T> {
        public void process(List<T> dataList) {
            dataList.parallelStream().forEach(entry -> {
                doIt();
            });
        }

        private void doIt() {
            String session = ContextHolder.get();
            log.info("doIt().session: {}", session);
        }
    }

    /**
     * 如果运气好，你会发现这样改又有问题，运气不好，这段代码在线下运行良好，这段代码就顺利上线了。不久你就会发现系统中会有一些其他很诡异的 bug。
     * <p>
     * 原因在于并行流的设计比较特殊，父线程也有可能参与到并行流线程池的调度，那如果上面的 process 方法被父线程执行，那么父线程的上下文会被清理。
     * 导致后续拷贝到子线程的上下文都为 null，同样产生丢失上下文的问题。
     *
     * @param <T>
     */
    @Slf4j
    static class ParallelProcessor2<T> {
        private String session;

        public ParallelProcessor2(String session) {
            this.session = session;
        }

        public void process(List<T> dataList) {
            dataList.parallelStream().forEach(entry -> {
                try {
                    ContextHolder.set(session);
                    // 业务处理
                    doIt();
                } catch (Exception e) {
                    log.error("error: {}", e.getMessage());
//                    log.error("error: {}, msg: {}", e, e.getMessage());
                } finally {
                    ContextHolder.remove();
                }
            });
        }

        private void doIt() {
            String session = ContextHolder.get();
            log.info("x2 doIt().session: {}", session);
        }
    }
}
