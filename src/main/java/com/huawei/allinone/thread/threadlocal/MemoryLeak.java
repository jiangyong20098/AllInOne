package com.huawei.allinone.thread.threadlocal;

import org.junit.Test;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *  ThreadLocal 容易用错的三个坑：
 *
 * 内存泄露
 *
 * 线程池中线程上下文丢失
 *
 * 并行流中线程上下文丢失
 */
public class MemoryLeak {
    /**
     * 内存泄露
     *
     * 由于 ThreadLocal 的 key 是弱引用，因此如果使用后不调用 remove 清理的话会导致对应的 value 内存泄露。
     *
     * 当 localCache 的值被重置之后 cacheInstance 被 ThreadLocalMap 中的 value 引用，无法被 GC，
     * 但是其 key 对 ThreadLocal 实例的引用是一个弱引用。
     *
     * 本来 ThreadLocal 的实例被 localCache 和 ThreadLocalMap 的 key 同时引用，但是当 localCache 的
     * 引用被重置之后，则 ThreadLocal 的实例只有ThreadLocalMap 的 key 这样一个弱引用了，此时这个实例在 GC 的时候能够被清理。
     *
     * ThreadLocal 本身对于 key 为 null 的 Entity 有自清理的过程，但是这个过程是依赖于后续对 ThreadLocal 的继续使用。
     *
     * 假如下面的这段代码是处于一个秒杀场景下，会有一个瞬间的流量峰值，这个流量峰值也会将集群的内存打到高位（或者运气不好的话直接将
     * 集群内存打满导致故障）。 后面由于峰值流量已过，对 ThreadLocal 的调用也下降，会使得 ThreadLocal 的自清理能力下降，造成内存泄露。
     *
     * ThreadLocal 的自清理是锦上添花，千万不要指望他雪中送碳。
     *
     * 相比于 ThreadLocal 中存储的 value 对象泄露，ThreadLocal 用在 web 容器中时更需要注意其引起的 ClassLoader 泄露。
     *
     */
    @Test
    public void testThreadLocalMemoryLeaks() {
        ThreadLocal<List<Integer>> localCache = new ThreadLocal<>();
        List<Integer> cacheInstance = new ArrayList<>(10000);
        localCache.set(cacheInstance);
        System.out.println("before remove: " + localCache.get());

        // 重置localCache前要调用localCache.remove();清理，否则会引起对应的value内存泄露
        localCache.remove();
        System.out.println("after remove: " + localCache.get());
        localCache = new ThreadLocal<>();
    }
}

/**
 * 相比于 ThreadLocal 中存储的 value 对象泄露，ThreadLocal用在 web 容器中时更需要注意其引起的 ClassLoader 泄露。
 *
 * Tomcat 官网对在 web 容器中使用 ThreadLocal 引起的内存泄露做了一个总结，详见：
 * https://cwiki.apache.org/confluence/display/tomcat/MemoryLeakProtection
 * 这里我们列举其中的一个例子，熟悉 Tomcat 的同学知道，Tomcat 中的 web 应用由 Webapp Classloader 这个类加载器的。
 * <p>
 * 并且 Webapp Classloader 是破坏双亲委派机制实现的，即所有的 web 应用先由 Webapp classloader 加载，
 * 这样的好处就是可以让同一个容器中的 web 应用以及依赖隔离。
 * <p>
 * 需要注意这个例子中的两个非常关键的点：
 * 1、MyCounter以及MyThreadLocal必须放到web应用的路径中，保被 Webapp Classloader 加载。
 * <p>
 * 2、ThreadLocal 类一定得是 ThreadLocal 的继承类，比如例子中的 MyThreadLocal，因为 ThreadLocal 本来被 Common Classloader 加载，
 * 其生命周期与 Tomcat 容器一致。ThreadLocal 的继承类包括比较常见的 NamedThreadLocal，注意不要踩坑。
 * <p>
 * 假如 LeakingServlet 所在的 Web 应用启动，MyThreadLocal 类也会被 Webapp Classloader 加载。
 * 如果此时 web 应用下线，而线程的生命周期未结束（比如为LeakingServlet 提供服务的线程是一个线程池中的线程）。
 * <p>
 * 那会导致 myThreadLocal 的实例仍然被这个线程引用，而不能被 GC，期初看来这个带来的问题也不大，因为 myThreadLocal 所引用的对象占用的
 * 内存空间不太多。
 * 问题在于 myThreadLocal 间接持有加载 web 应用的 webapp classloader 的引用（通过 myThreadLocal.getClass().getClassLoader()
 * 可以引用到）。
 * 而加载 web 应用的 webapp classloader 又持有它加载的所有类的引用，这就引起了 Classloader 泄露，它泄露的内存就非常可观了。
 */
class LeakingServlet extends HttpServlet {
    private static MyThreadLocal myThreadLocal = new MyThreadLocal();

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {

        MyCounter counter = myThreadLocal.get();
        if (counter == null) {
            counter = new MyCounter();
            myThreadLocal.set(counter);
        }

        response.getWriter().println(
                "The current thread served this servlet " + counter.getCount()
                        + " times");
        counter.increment();
    }
}

class MyCounter {
    private int count = 0;

    public void increment() {
        count++;
    }

    public int getCount() {
        return count;
    }
}

class MyThreadLocal extends ThreadLocal<MyCounter> {
}



