package cn.didadu;

import org.I0Itec.zkclient.ZkClient;

//ZkClient删除节点数据
public class DelDataSample {
	public static void main(String[] args) {
    	ZkClient zkClient = new ZkClient("localhost:2181", 2000);
        /**
         * 之间已经创建了节点/zk-boot/c1
         * 该接口可以递归删除节点
         */
        zkClient.deleteRecursive("/zk-book");
    }
}