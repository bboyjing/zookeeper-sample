package cn.didadu;

import org.I0Itec.zkclient.ZkClient;

//ZkClient检测节点是否存在
public class ExistNodeSample {
    public static void main(String[] args) throws Exception {
        String path = "/zk-book";
        ZkClient zkClient = new ZkClient("localhost:2181", 2000);
        System.out.println("Node " + path + " exists " + zkClient.exists(path));
    }
}