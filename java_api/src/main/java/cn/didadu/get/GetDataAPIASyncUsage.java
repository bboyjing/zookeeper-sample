package cn.didadu.get;

import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

// ZooKeeper API 获取节点数据内容，使用异步(async)接口。
public class GetDataAPIASyncUsage implements Watcher {
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    protected static CountDownLatch nodeDataChangedSemaphore = new CountDownLatch(2);
    private static ZooKeeper zk;

    public static void main(String[] args) throws Exception {
    	String path = "/zk-book";
    	zk = new ZooKeeper("localhost:2181", 5000, new GetDataAPIASyncUsage());
        connectedSemaphore.await();

        //创建节点
        zk.create( path, "123".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL );

        //异步获取节点数据
        zk.getData( path, true, new IDataCallback(), null );

        //修改节点的值
        zk.setData(path, "123".getBytes(), -1 );
        nodeDataChangedSemaphore.await();
    }

    @Override
    public void process(WatchedEvent event) {
        if (KeeperState.SyncConnected == event.getState()) {
            if (EventType.None == event.getType() && null == event.getPath()) {
                connectedSemaphore.countDown();
            } else if (event.getType() == EventType.NodeDataChanged) {
                try {
                    zk.getData( event.getPath(), true, new IDataCallback(), null );
                } catch (Exception e) {}
            }
        }
    }
}

class IDataCallback implements AsyncCallback.DataCallback{

    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        System.out.println(rc + ", " + path + ", " + new String(data));
        System.out.println(stat.getCzxid()+","+
                  		   stat.getMzxid()+","+
		                   stat.getVersion());
        GetDataAPIASyncUsage.nodeDataChangedSemaphore.countDown();
    }
}

