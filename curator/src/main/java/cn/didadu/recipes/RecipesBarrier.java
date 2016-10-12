package cn.didadu.recipes;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class RecipesBarrier {
    static String barrierPath = "/curator_recipes_barrier_path";
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    CuratorFramework client = CuratorFrameworkFactory.builder()
                            .connectString("localhost:2181")
                            .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
                    client.start();
                    DistributedDoubleBarrier barrier = new DistributedDoubleBarrier(client,barrierPath,5);
                    Thread.sleep(Math.round(Math.random() * 3000));

                    //enter方法进入等待
                    System.out.println(Thread.currentThread().getName() + "号进入barrier");
                    barrier.enter();

                    //当等待进入的成员达到预期数量后，同时启动
                    System.out.println("启动...");
                    Thread.sleep(Math.round(Math.random() * 3000));

                    //leave再次同时进入等待退出状态
                    barrier.leave();

                    //当等待进入的成员达到预期数量后，同时退出
                    System.out.println("退出...");
                } catch (Exception e) {
                }
            }).start();
        }
    }
}