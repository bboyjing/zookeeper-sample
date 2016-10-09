package cn.didadu.delete;

import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import java.util.concurrent.CountDownLatch;

// ZooKeeper API 删除节点，使用同步(sync)接口。
public class DeleteAPISyncUsage implements Watcher {

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        ZooKeeper zk = new ZooKeeper("localhost:2181", 5000, new DeleteAPISyncUsage());
        connectedSemaphore.await();

        /**
         * 删除节点，需要注意的是至允许删除叶子节点
         */
        zk.delete("/persistent-node", -1);
    }

    @Override
    public void process(WatchedEvent event) {
        if (KeeperState.SyncConnected == event.getState()) {
            if (EventType.None == event.getType() && null == event.getPath()) {
                connectedSemaphore.countDown();
            }
        }
    }
}