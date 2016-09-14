package cn.didadu.auth;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

//使用不同的权限信息的ZooKeeper会话访问含权限信息的数据节点
public class GetFooNodeByAuth implements Watcher{
    private static CountDownLatch noAuthSemaphore = new CountDownLatch(1);
    private static CountDownLatch wrongAuthSemaphore = new CountDownLatch(1);
    private static CountDownLatch rightAuthSemaphore = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        String path = "/zk-book-auth_test";

        try{
            ZooKeeper noAuthZK = new ZooKeeper("localhost:2181", 50000, new GetFooNodeByAuth());
            noAuthSemaphore.await();
            //使用不包含权限信息的客户端访问节点，抛出异常
            noAuthZK.getData( path, false, null );
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            ZooKeeper wrongAuthZK = new ZooKeeper("localhost:2181", 50000, new GetFooNodeByAuth());
            wrongAuthSemaphore.await();
            wrongAuthZK.addAuthInfo("digest", "bar:true".getBytes());
            //使用错误的权限信息访问节点，抛出异常
            wrongAuthZK.getData( path, false, null );
        }catch (Exception e){
            e.printStackTrace();
        }

        ZooKeeper rightAuthZK = new ZooKeeper("localhost:2181", 50000, new GetFooNodeByAuth());
        rightAuthSemaphore.await();
        rightAuthZK.addAuthInfo("digest", "foo:true".getBytes());
        //使用正确的权限信息获取节点
        System.out.println(new String(rightAuthZK.getData( path, false, null )));

    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (Event.KeeperState.SyncConnected == watchedEvent.getState()) {
            if(noAuthSemaphore.getCount() > 0){
                noAuthSemaphore.countDown();
                return;
            }
            if(wrongAuthSemaphore.getCount() > 0){
                wrongAuthSemaphore.countDown();
                return;
            }
            if(rightAuthSemaphore.getCount() > 0){
                rightAuthSemaphore.countDown();
                return;
            }
        }
    }
}