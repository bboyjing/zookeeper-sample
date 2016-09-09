package cn.didadu.get;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.concurrent.CountDownLatch;

// ZooKeeper API 获取子节点列表，使用同步(sync)接口。
public class ZooKeeperGetChildrenAPISyncUsage implements Watcher {

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private static CountDownLatch watcherSemaphore = new CountDownLatch(1);
    private static ZooKeeper zk = null;
    
    public static void main(String[] args) throws Exception{
        /**
         * 声明node路径
         * 实例化Zookeeper
         */
    	String path = "/zk-book";
        zk = new ZooKeeper("localhost:2181", 500000, new ZooKeeperGetChildrenAPISyncUsage());
        connectedSemaphore.await();

        /**
         * 创建永久节点/zk-book
         * 创建临时节点/zk-book/c1
         */
        zk.create(path, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zk.create(path + "/c1", "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        /**
         * 获取/zk-book下的子节点
         * 此时注册了默认的watch，如果继续在/zk-book下增加节点的话，会调用process方法，通知客户端节点变化了
         * 但是仅仅是发通知，客户端需要自己去再次查询
         * 另外需要注意的是watcher是一次性的，即一旦触发一次通知后，该watcher就失效了，需要反复注册watcher，
         * 即process方中的getChildren继续注册了watcher
         */
        List<String> childrenList = zk.getChildren(path, true);
        System.out.println(childrenList);
        
        zk.create(path+"/c2", "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        watcherSemaphore.await();
    }

    public void process(WatchedEvent event) {
      if (KeeperState.SyncConnected == event.getState()) {
        if (EventType.None == event.getType() && null == event.getPath()) {
            connectedSemaphore.countDown();
        } else if (event.getType() == EventType.NodeChildrenChanged) {
            try {
                //收到子节点变更通知，重新主动查询子节点信息
                System.out.println("ReGet Child:" + zk.getChildren(event.getPath(),true));
                watcherSemaphore.countDown();
            } catch (Exception e) {}
        }
      }
    }
}
