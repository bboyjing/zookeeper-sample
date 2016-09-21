package cn.didadu;
import org.I0Itec.zkclient.ZkClient;

// 使用ZkClient创建节点
public class CreateNodeSample {
    public static void main(String[] args) {
    	ZkClient zkClient = new ZkClient("localhost:2181", 5000);
        //ZKClient可以递归创建父节点
        zkClient.createPersistent("/zk-book/c1", true);
    }
}