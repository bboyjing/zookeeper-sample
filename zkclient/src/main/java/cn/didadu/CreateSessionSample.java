package cn.didadu;

import org.I0Itec.zkclient.ZkClient;

import java.io.IOException;

// 使用ZkClient来创建一个ZooKeeper客户端
public class CreateSessionSample {
    public static void main(String[] args) {
        //ZkClient通过内部封装，将zookeeper异步的会话过程同步化了
        ZkClient zkClient = new ZkClient("localhost:2181", 5000);
        System.out.println("ZooKeeper session established.");
    }
}