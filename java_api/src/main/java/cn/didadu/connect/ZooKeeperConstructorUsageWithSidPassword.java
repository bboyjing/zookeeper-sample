package cn.didadu.connect;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

//Java API -> 创建连接 -> 创建一个最基本的ZooKeeper对象实例，复用sessionId和password
public class ZooKeeperConstructorUsageWithSidPassword implements Watcher {
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        ZooKeeper zookeeper = new ZooKeeper("localhost:2181", 5000,
                new ZooKeeperConstructorUsageWithSidPassword());
        connectedSemaphore.await();

        /**
         * 获取sessionId、password，目的是为了复用会话
         */
        long sessionId = zookeeper.getSessionId();
        byte[] password = zookeeper.getSessionPasswd();

        //使用错误的sessionId和password连接
        zookeeperConnector wrong = new zookeeperConnector(1, "test".getBytes(), new CountDownLatch(1));
        wrong.connect();

        //使用正确的sessionId和password连接
        zookeeperConnector correct = new zookeeperConnector(sessionId, password, new CountDownLatch(1));
        correct.connect();

    }

    public void process(WatchedEvent event) {
        System.out.println("Receive watched event：" + event);
        if (KeeperState.SyncConnected == event.getState()) {
            connectedSemaphore.countDown();
        }
    }

    static class zookeeperConnector implements Watcher {
        private long sessionId;
        private byte[] password;
        private CountDownLatch connectedSemaphore;

        public zookeeperConnector(long sessionId, byte[] password, CountDownLatch connectedSemaphore) {
            this.sessionId = sessionId;
            this.password = password;
            this.connectedSemaphore = connectedSemaphore;
        }

        public void connect() throws IOException, InterruptedException {
            new ZooKeeper("localhost:2181", 5000, this, sessionId, password);
            this.connectedSemaphore.await();
        }

        @Override
        public void process(WatchedEvent watchedEvent) {
            System.out.println("Receive watched event：" + watchedEvent);
            this.connectedSemaphore.countDown();
        }
    }
}