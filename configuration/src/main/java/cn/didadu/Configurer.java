package cn.didadu;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

/**
 * Created by zhangjing on 16-10-17.
 */

@Configuration
public class Configurer{
    @Bean
    public String zookeeperLoader() throws Exception {
        String hostname = InetAddress.getLocalHost().getHostName().split("\\.")[0];

        CuratorFramework zkClient = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181,localhost:2182,localhost:2183")
                .sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        zkClient.start();

        StringBuilder basePath = new StringBuilder("/configuration/").append(hostname).append("/");

        StringBuilder urlPath = new StringBuilder(basePath).append("spring.datasource.url");
        System.out.println(new String(zkClient.getData().forPath(urlPath.toString())));

        StringBuilder namePath = new StringBuilder(basePath).append("spring.datasource.name");
        System.out.println(new String(zkClient.getData().forPath(namePath.toString())));

        StringBuilder pwdPath = new StringBuilder(basePath).append("spring.datasource.password");
        System.out.println(new String(zkClient.getData().forPath(pwdPath.toString())));

        zkClient.close();
        return "ok";
    }
}
