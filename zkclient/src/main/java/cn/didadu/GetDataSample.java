package cn.didadu;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

//ZkClient获取节点数据
public class GetDataSample {
    public static void main(String[] args) throws Exception {
        String path = "/zk-book";
        ZkClient zkClient = new ZkClient("localhost:2181", 5000);
        zkClient.createEphemeral(path, "123");

        /**
         * 注册Listenner
         * Listener中有两个方法，一个对应节点数据更改，一个对应节点数据删除
         */
        zkClient.subscribeDataChanges(path, new IZkDataListener() {
            public void handleDataDeleted(String dataPath) throws Exception {
                System.out.println("Node " + dataPath + " deleted.");
            }

            public void handleDataChange(String dataPath, Object data) throws Exception {
                System.out.println("Node " + dataPath + " changed, new data: " + data);
            }
        });

        /**
         * 获取节点数据，并建立监听
         */
        System.out.println(zkClient.readData(path).toString());

        /**
         * 更改数据，触发handleDataChange
         */
        zkClient.writeData(path, "456");
        Thread.sleep(1000);

        /**
         * 删除节点，触发handleDataDeleted
         */
        zkClient.delete(path);
        Thread.sleep(Integer.MAX_VALUE);
    }
}