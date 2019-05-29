package com.youjian.xunwu.qiniu.service;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;

import java.io.File;
import java.io.InputStream;

/**
 * 七牛云服务
 */
public interface IQiniuService {
    /**
     * 文件上传
     * @param file
     * @return
     * @throws QiniuException
     */
    Response uploadFile(File file) throws QiniuException;

    /**
     * 文件流上传
     * @param inputStream
     * @return
     * @throws QiniuException
     */
    Response uploadFile(InputStream inputStream) throws QiniuException;

    /**
     * 文件删除
     */
    Response deleteFile(String key) throws QiniuException;
}
