package cn.didadu;

import org.I0Itec.zkclient.ZkClient;

//ZkClient更新节点数据
public class SetDataSample {
    public static void main(String[] args) throws Exception {
        String path = "/zk-book";
        ZkClient zkClient = new ZkClient("localhost:2181", 2000);
        zkClient.createEphemeral(path, new Integer(1));
        zkClient.writeData(path, new Integer(1));
    }
}