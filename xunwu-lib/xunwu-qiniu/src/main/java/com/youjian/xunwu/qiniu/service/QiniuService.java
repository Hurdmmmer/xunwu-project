package com.youjian.xunwu.qiniu.service;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.youjian.xunwu.qiniu.config.QiniuConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

/**
 * 七牛云服务实现类
 */
@Service
@Slf4j
@EnableConfigurationProperties(QiniuConfig.class)
public class QiniuService implements IQiniuService, InitializingBean {

    @Autowired
    private QiniuConfig qiniuConfig;

    @Autowired
    private UploadManager uploadManager;

    @Autowired
    private BucketManager bucketManager;

    @Autowired
    private Auth auth;

    // 七牛云返回对象配置
    private StringMap putPolicy;

    @Override
    public Response uploadFile(File file) throws QiniuException {
        Response response = this.uploadManager.put(file, null, getUploadToken());
        int retry = 0;
        while (retry++ < 3 && response.needRetry()) {
            response = this.uploadManager.put(file, null, getUploadToken());
        }
        return response;
    }

    @Override
    public Response uploadFile(InputStream inputStream) throws QiniuException {
        Response response = this.uploadManager.put(inputStream, null, getUploadToken(), null, null);
        int retry = 0;
        while (retry++ < 3 && response.needRetry()) {
            response = this.uploadManager.put(inputStream, null, getUploadToken(), null, null);
        }
        if (response.isOK()) {
            String url = auth.privateDownloadUrl(this.qiniuConfig.getCdnPrefix());
            log.info("访问的外链是: "+url);
        }
        return response;
    }

    @Override
    public Response deleteFile(String key) throws QiniuException {

        Response response = this.bucketManager.delete(this.qiniuConfig.getBucket(), key);
        int retry = 0;
        while (retry++ < 3 && response.needRetry()) {
            response = this.bucketManager.delete(this.qiniuConfig.getBucket(), key);
        }
        return response;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        putPolicy = new StringMap();
        putPolicy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"width\":$(imageInfo.width)," +
                "\"height\":$(imageInfo.height)}");
    }


    /**
     * 获取上传凭证
     */
    public String getUploadToken() {
        return auth.uploadToken(qiniuConfig.getBucket(), null, 3600, putPolicy);
    }
}
