package cn.didadu.connect;


import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

//Java API -> 创建连接 -> 创建一个最基本的ZooKeeper对象实例
public class ZooKeeperConstructorUsageSimple implements Watcher {
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        /**
         * Zookeeper客户端和服务端会话的建立是一个异步的过程
         * 也就是说在程序中，构造方法会在处理完客户端初始化工作后立即返回
         * 在大多数情况下此时并没有真正建立好一个可用的会话，此时在会话的生命周期中处于“CONNECTING”的状态
         */
        ZooKeeper zookeeper = new ZooKeeper("localhost:2181", 5000,
                new ZooKeeperConstructorUsageSimple());
        System.out.println(zookeeper.getState());

        try {
            //等待Watcher通知SyncConnected
            connectedSemaphore.await();
        } catch (InterruptedException e) {
        }
        System.out.println("ZooKeeper session established.");
    }

    /**
     * ZooKeeper_Constructor_Usage_Simples实现了Watcher接口，重写了process方法
     * 该方法负责处理来自Zookeeper服务端的Watcher通知，即服务端建立连接后会调用该方法
     *
     * @param event
     */
    public void process(WatchedEvent event) {
        System.out.println("Receive watched event：" + event);
        if (Event.KeeperState.SyncConnected == event.getState()) {
            connectedSemaphore.countDown();
        }
    }
}