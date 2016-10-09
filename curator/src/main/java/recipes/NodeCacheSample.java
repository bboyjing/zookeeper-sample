package recipes;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class NodeCacheSample {
    static String path = "/zk-book/nodecache";
    static CuratorFramework client = CuratorFrameworkFactory.builder()
            .connectString("localhost:2181")
            .sessionTimeoutMs(5000)
            .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();

    public static void main(String[] args) throws Exception {
        client.start();

        final NodeCache cache = new NodeCache(client, path, false);
        cache.start(true);
        cache.getListenable().addListener(() -> {
            System.out.println("NodeCacheListener...");
            if (cache.getCurrentData() != null) {
                System.out.println("Node data update, new data: " +
                        new String(cache.getCurrentData().getData()));
            }
        });

        //创建节点会触发NodeCacheListener
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path, "init".getBytes());
        Thread.sleep(1000);

        /**
         * 修改节点会触发NodeCacheListener
         * 但是只会输出"y"，所以猜测NodeCache不适用并发修改场景
         */
        client.setData().forPath(path, "x".getBytes());
        client.setData().forPath(path, "y".getBytes());
        Thread.sleep(1000);

        //该版本删除节点会触发NodeCacheListener
        client.delete().deletingChildrenIfNeeded().forPath(path);
        Thread.sleep(1000);
    }
}