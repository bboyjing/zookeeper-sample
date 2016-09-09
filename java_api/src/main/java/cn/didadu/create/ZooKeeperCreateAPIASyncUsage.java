package cn.didadu.create;

import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;

import java.util.concurrent.CountDownLatch;

// ZooKeeper API创建节点，使用异步(async)接口。
public class ZooKeeperCreateAPIASyncUsage implements Watcher {

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {

        ZooKeeper zookeeper = new ZooKeeper("localhost:2181", 5000,
                new ZooKeeperCreateAPIASyncUsage());
        connectedSemaphore.await();


        CountDownLatch createSemaphore = new CountDownLatch(3);
        /**
         * 异步创建节点
         * 接口不会抛出异常，而是通过回调函数的rc参数体现：-110表示节点已存在
         */
        zookeeper.create("/zk-test-ephemeral-", "".getBytes(),
                Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL,
                new IStringCallback(createSemaphore), "I am context.");

        zookeeper.create("/zk-test-ephemeral-", "".getBytes(),
                Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL,
                new IStringCallback(createSemaphore), "I am context.");

        zookeeper.create("/zk-test-ephemeral-", "".getBytes(),
                Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL,
                new IStringCallback(createSemaphore), "I am context.");

        createSemaphore.await();
    }

    public void process(WatchedEvent event) {
        if (KeeperState.SyncConnected == event.getState()) {
            connectedSemaphore.countDown();
        }
    }
}

class IStringCallback implements AsyncCallback.StringCallback {

    private CountDownLatch createSemaphore;

    public IStringCallback(CountDownLatch createSemaphore){
        this.createSemaphore = createSemaphore;
    }

    public void processResult(int rc, String path, Object ctx, String name) {
        System.out.println("Create path result: [" + rc + ", " + path + ", "
                + ctx + ", real path name: " + name + "]");
        createSemaphore.countDown();
    }
}