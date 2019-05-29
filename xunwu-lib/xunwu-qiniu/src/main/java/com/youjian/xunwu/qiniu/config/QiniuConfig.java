package com.youjian.xunwu.qiniu.config;

import com.google.gson.Gson;
import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "qiniu")
@PropertySource("classpath:QiniuConfig.properties")
@Data
public class QiniuConfig {
    private String accessKey;
    private String secretKey;
    private String cdnPrefix;
    private String bucket;


    @Bean
    public com.qiniu.storage.Configuration configuration() {
        // 设置上传华东机房
        return new com.qiniu.storage.Configuration(Zone.zone0());
    }

    // 七牛云上传工具实例
    @Bean
    public UploadManager uploadManager() {
        return new UploadManager(configuration());
    }

    /**
     * 七牛云认证信息
     */
    @Bean
    public Auth auth() {
        return Auth.create(accessKey, secretKey);
    }
    // 七牛云空间管理实例
    @Bean
    public BucketManager bucketManager() {
        return new BucketManager(auth(), configuration());
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }
}

