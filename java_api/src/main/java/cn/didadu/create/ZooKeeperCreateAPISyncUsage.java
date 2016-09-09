package cn.didadu.create;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

//ZooKeeper API创建节点，使用同步(sync)接口。
public class ZooKeeperCreateAPISyncUsage implements Watcher {

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public static void main(String[] args) throws Exception{
        ZooKeeper zookeeper = new ZooKeeper("localhost:2181", 5000,
				new ZooKeeperCreateAPISyncUsage());
        connectedSemaphore.await();

        /**
         * 创建临时节点
         */
        String path1 = zookeeper.create("/zk-test-ephemeral-", "".getBytes(),
        		Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        System.out.println("Success create znode: " + path1);

        /**
         * 创建临时顺序节点
         * Zookeeper会自动在节点后缀加上一个数字
         */
        String path2 = zookeeper.create("/zk-test-ephemeral-", "".getBytes(),
        		Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Success create znode: " + path2);

        /**
         * 创建永久节点
         */
        String path3 = zookeeper.create("/persistent-node","bboyjing".getBytes(),
                Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println("Success create znode: " + path3);
    }

    public void process(WatchedEvent event) {
        if (KeeperState.SyncConnected == event.getState()) {
            connectedSemaphore.countDown();
        }
    }
}
