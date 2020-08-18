package com.example.cameratest.annotation;

import android.graphics.Bitmap;

/**
 *  @Author wonderful
 *  @Date 2020-5-22
 *  @Version 1.0
 *  @Description 相机数据运输接口，只为相机数据的传递服务
 */
public interface CameraDataTransport {

    /**
     * 运输拍照数据
     * @param bitmap 拍照转换后的bitmap
     * @param data 拍照原始数据
     */
    public void picture(Bitmap bitmap,byte[] data);
}
