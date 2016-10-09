package cn.didadu.set;

import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

// ZooKeeper API 更新节点数据内容，使用同步(sync)接口。
public class SetDataAPISyncUsage implements Watcher {
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {

        String path = "/zk-book";
        ZooKeeper zk = new ZooKeeper("localhost:2181", 5000, new SetDataAPISyncUsage());
        connectedSemaphore.await();

        zk.create(path, "123".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        /**
         * version：-1，代表不需要根据版本号更新
         */
        Stat stat = zk.setData(path, "456".getBytes(), -1);
        System.out.println(stat.getCzxid() + "," +
                stat.getMzxid() + "," +
                stat.getVersion());

        /**
         * 根据上一次更新的版本号更新，成功
         */
        Stat stat2 = zk.setData(path, "456".getBytes(), stat.getVersion());
        System.out.println(stat2.getCzxid() + "," +
                stat2.getMzxid() + "," +
                stat2.getVersion());
        /**
         * 根据上上次旧的版本跟新，失败抛异常
         */
        try {
            zk.setData(path, "456".getBytes(), stat.getVersion());
        } catch (KeeperException e) {
            System.out.println("Error: " + e.code() + "," + e.getMessage());
        }
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