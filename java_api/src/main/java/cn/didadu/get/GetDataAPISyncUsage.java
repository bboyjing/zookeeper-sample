package cn.didadu.get;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

// ZooKeeper API 获取节点数据内容，使用同步(sync)接口。
public class GetDataAPISyncUsage implements Watcher {
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
	private static CountDownLatch nodeDataChangedSemaphore = new CountDownLatch(1);
    private static ZooKeeper zk = null;
    private static Stat stat = new Stat();

    public static void main(String[] args) throws Exception {

    	String path = "/zk-book";
    	zk = new ZooKeeper("localhost:2181", 5000, new GetDataAPISyncUsage());
        connectedSemaphore.await();

	    /**
	     * 新增节点并给节点赋值
	     */
        zk.create( path, "123".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL );

	    /**
	     * 获取节点数据，传入旧的stat，会被服务端响应的新的stat替换
	     */
        System.out.println(new String(zk.getData( path, true, stat )));
        System.out.println(stat.getCzxid()+","+stat.getMzxid()+","+stat.getVersion());

	    /**
	     * 虽然节点的值没有改版，但是版本号改变了，依然会触发process事件
	     */
        zk.setData( path, "123".getBytes(), -1 );
		nodeDataChangedSemaphore.await();
	}

	@Override
    public void process(WatchedEvent event) {
        if (KeeperState.SyncConnected == event.getState()) {
			if (EventType.None == event.getType() && null == event.getPath()) {
  	          connectedSemaphore.countDown();
			} else if (event.getType() == EventType.NodeDataChanged) {
				try {
					System.out.println(new String(zk.getData( event.getPath(), true, stat)));
					System.out.println(stat.getCzxid()+","+ stat.getMzxid()+","+ stat.getVersion());
					nodeDataChangedSemaphore.countDown();
				} catch (Exception e) {}
			}
		}
	}
}