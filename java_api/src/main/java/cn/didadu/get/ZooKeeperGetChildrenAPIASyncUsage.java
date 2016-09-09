package cn.didadu.get;

import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.concurrent.CountDownLatch;

//ZooKeeper API 获取子节点列表，使用异步(ASync)接口。
public class ZooKeeperGetChildrenAPIASyncUsage implements Watcher {

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private static CountDownLatch watcherSemaphore = new CountDownLatch(1);
    private static ZooKeeper zk = null;

    public static void main(String[] args) throws Exception{
    	String path = "/zk-book";
        zk = new ZooKeeper("localhost:2181", 5000, new ZooKeeperGetChildrenAPIASyncUsage());
        connectedSemaphore.await();

        /**
         * 新增/zk-book、/zk-book/c1节点
         */
        zk.create(path, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zk.create(path+"/c1", "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        /**
         * 异步获取节点信息
         * 可以用在启动的时候获取配置信息而不影响主流程
         */
        zk.getChildren(path, true, new IChildrenCallback(), null);
        
        zk.create(path+"/c2", "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        watcherSemaphore.await();
    }

    public void process(WatchedEvent event) {
      if (KeeperState.SyncConnected == event.getState()) {
	      if (EventType.None == event.getType() && null == event.getPath()) {
	          connectedSemaphore.countDown();
	      } else if (event.getType() == EventType.NodeChildrenChanged) {
	          try {
	              System.out.println("ReGet Child:"+zk.getChildren(event.getPath(),true));
                  watcherSemaphore.countDown();
	          } catch (Exception e) {}
	      }
	    }
     }
}

class IChildrenCallback implements AsyncCallback.Children2Callback{
	public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        System.out.println("Get Children znode result: [response code: " + rc + ", param path: " + path
                + ", ctx: " + ctx + ", children list: " + children + ", stat: " + stat);
    }
}