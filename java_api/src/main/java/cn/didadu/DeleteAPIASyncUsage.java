package cn.didadu;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

// ZooKeeper API 删除节点，使用一异步(async)接口。
public class DeleteAPIASyncUsage implements Watcher{
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {

        ZooKeeper zk = new ZooKeeper("localhost:2181", 5000, new DeleteAPISyncUsage());
        connectedSemaphore.await();

        /**
         * 删除节点，需要注意的是至允许删除叶子节点
         */
        CountDownLatch deleteSemaphore = new CountDownLatch(1);
        zk.delete("/persistent-node", -1, new IVoidCallback(deleteSemaphore), "delete");
        deleteSemaphore.await();
    }
    @Override
    public void process(WatchedEvent event) {
        if (Event.KeeperState.SyncConnected == event.getState()) {
            if (Event.EventType.None == event.getType() && null == event.getPath()) {
                connectedSemaphore.countDown();
            }
        }
    }
}

class IVoidCallback implements AsyncCallback.VoidCallback {

    private CountDownLatch createSemaphore;

    public IVoidCallback(CountDownLatch createSemaphore){
        this.createSemaphore = createSemaphore;
    }

    @Override
    public void processResult(int rc, String path, Object ctx) {
        System.out.println("Create path result: [" + rc + ", " + path + ", "
                + ctx);
        createSemaphore.countDown();
    }
}
