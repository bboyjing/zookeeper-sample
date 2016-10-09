package cn.didadu;

import org.I0Itec.zkclient.ZkClient;


// ZkClient获取子节点列表。
public class GetChildrenSample {
    public static void main(String[] args) throws Exception {
        String path = "/zk-book";
        ZkClient zkClient = new ZkClient("localhost:2181", 5000);

        /**
         * 注册回调接口
         * Listener不是一次性的，注册一次就会一直生效
         */
        zkClient.subscribeChildChanges(path, (parentPath, currentChildren) -> {
            System.out.println(parentPath + " 's child changed, currentChildren:" + currentChildren);
        });

        /**
         * 第一次创建当前节点，客户端会收到通知
         * /zk-book 's child changed, currentChildren:[]
         */
        zkClient.createPersistent(path);
        Thread.sleep(1000);

        /**
         * 创建子节点，客户端会收到通知
         * /zk-book 's child changed, currentChildren:[c1]
         */
        zkClient.createPersistent(path + "/c1");
        Thread.sleep(1000);

        /**
         * 删除子节点，客户端会收到通知
         * /zk-book 's child changed, currentChildren:[]
         */
        zkClient.delete(path + "/c1");
        Thread.sleep(1000);

        /**
         * 删除当前节点，客户端会收到通知
         * /zk-book 's child changed, currentChildren:null
         */
        zkClient.delete(path);
        Thread.sleep(Integer.MAX_VALUE);
    }
}