package cn.didadu.set;

import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

// ZooKeeper API 更新节点数据内容，使用异步(async)接口。
public class SetDataAPIASyncUsage implements Watcher {
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        String path = "/zk-book";
        ZooKeeper zk = new ZooKeeper("localhost:2181", 5000, new SetDataAPIASyncUsage());
        connectedSemaphore.await();

        zk.create(path, "123".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        CountDownLatch setSemaphore = new CountDownLatch(1);
        zk.setData(path, "456".getBytes(), -1, new IStatCallback(setSemaphore), null);
        setSemaphore.await();

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

class IStatCallback implements AsyncCallback.StatCallback {
    private CountDownLatch setSemaphore;

    public IStatCallback(CountDownLatch setSemaphore) {
        this.setSemaphore = setSemaphore;
    }

    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if (rc == 0) {
            System.out.println("SUCCESS");
            setSemaphore.countDown();
        }
    }
}