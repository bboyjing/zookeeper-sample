package cn.didadu.auth;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

//使用含权限信息的ZooKeeper会话创建数据节点
public class ZNodeForFoo implements Watcher {
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        String path = "/zk-book-auth_test";
        ZooKeeper zookeeper = new ZooKeeper("localhost:2181", 50000, new ZNodeForFoo());
        connectedSemaphore.await();
        //添加带权限信息的节点
        zookeeper.addAuthInfo("digest", "foo:true".getBytes());
        zookeeper.create(path, "init".getBytes(), Ids.CREATOR_ALL_ACL, CreateMode.PERSISTENT);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (Event.KeeperState.SyncConnected == watchedEvent.getState()) {
            connectedSemaphore.countDown();
        }
    }
}