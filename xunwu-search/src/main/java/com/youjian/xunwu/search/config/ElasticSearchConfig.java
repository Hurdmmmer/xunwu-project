package com.youjian.xunwu.search.config;

import lombok.Data;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *  es 配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticSearchConfig {
    private String host;
    private int port;
    private String cluster;
    private boolean clientTransportSniff;

    @Bean
    public TransportClient esClient() throws UnknownHostException {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        Settings settings = Settings.builder()
                .put("cluster.name", cluster)
                .put("client.transport.sniff", clientTransportSniff) // 自动发现 es 节点
                .build();


        TransportAddress master = new TransportAddress( InetAddress.getByName(host), port);
        // 多个节点继续 addTransportAddress 加入 ip
        return new PreBuiltTransportClient(settings)
                .addTransportAddress(master);
        //      .addTransportAddress(加入多个节点的 ip 封装对象)
    }
}
