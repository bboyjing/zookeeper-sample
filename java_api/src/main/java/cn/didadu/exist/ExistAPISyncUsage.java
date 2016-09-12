package cn.didadu.exist;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

// ZooKeeper API 判断节点是否存在，使用同步(sync)接口。
public class ExistAPISyncUsage implements Watcher {
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private static CountDownLatch lastSemaphore = new CountDownLatch(1);
    private static ZooKeeper zk;
    private static String path = "/zk-book";

    public static void main(String[] args) throws Exception {
    	zk = new ZooKeeper("localhost:2181", 5000, new ExistAPISyncUsage());
    	connectedSemaphore.await();

        /**
         * 通过exists接口检测是否存在指定节点，同事注册一个Watcher
         */
    	zk.exists( path, true );

        /**
         * 创建节点/zk-book，服务器会向客户端发送事件通知：NodeCreated
         * 客户端收到通知后，继续调用exists接口，注册Watcher
         */
    	zk.create( path, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT );

        /**
         * 更新节点数据，服务器会向客户端发送事件通知：NodeDataChanged
         * 客户端收到通知后，继续调用exists接口，注册Watcher
         */
    	zk.setData( path, "123".getBytes(), -1 );

        /**
         * 删除节点/zk-book
         * 客户端会收到服务端的事件通知：NodeDeleted
         */
    	zk.delete( path, -1 );

        lastSemaphore.await();
    }

    @Override
    public void process(WatchedEvent event) {
        try {
            if (KeeperState.SyncConnected == event.getState()) {
                if (EventType.None == event.getType() && null == event.getPath()) {
                    connectedSemaphore.countDown();
                } else if (EventType.NodeCreated == event.getType()) {
                    System.out.println("Node(" + event.getPath() + ")Created");
                    zk.exists( event.getPath(), true );
                } else if (EventType.NodeDeleted == event.getType()) {
                    System.out.println("Node(" + event.getPath() + ")Deleted");
                    zk.exists( event.getPath(), true );
                    System.out.println("Last semaphore");
                    lastSemaphore.countDown();
                } else if (EventType.NodeDataChanged == event.getType()) {
                    System.out.println("Node(" + event.getPath() + ")DataChanged");
                    zk.exists( event.getPath(), true );
                }
            }
        } catch (Exception e) {}
    }
}