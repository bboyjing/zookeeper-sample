package cn.didadu.curatorApi;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//使用Curator的异步接口
public class CreateNodeBackgroundSample {
    static String path = "/zk-book";
    static CuratorFramework client = CuratorFrameworkFactory.builder()
            .connectString("localhost:2181")
            .sessionTimeoutMs(5000)
            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
            .namespace("base").build();
    static CountDownLatch semaphore = new CountDownLatch(2);
    static ExecutorService tp = Executors.newFixedThreadPool(2);

    public static void main(String[] args) throws Exception {
    	client.start();
        System.out.println("Main thread: " + Thread.currentThread().getName());

        /**
         * 此处传入了自定义的Executor
         * 任务将由传入的ExecutorService去处理
         */
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                .inBackground((client1, event) -> {
            System.out.println("event[code: " + event.getResultCode() + ", type: " + event.getType() + "]");
            System.out.println("Thread of processResult: " + Thread.currentThread().getName());
            semaphore.countDown();
        }, tp).forPath(path, "init".getBytes());

        //
        /**
         * 此处没有传入自定义的Executor
         * 使用Zookeeper默认的EventThread来处理，此处是名为main-EventThread的线程
         */
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                .inBackground((client12, event) -> {
            System.out.println("event[code: " + event.getResultCode() + ", type: " + event.getType() + "]");
            System.out.println("Thread of processResult: " + Thread.currentThread().getName());
            semaphore.countDown();
        }).forPath(path, "init".getBytes());

        semaphore.await();
        tp.shutdown();
    }
}