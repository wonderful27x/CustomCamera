package com.example.cameratest;

import android.graphics.Bitmap;

/**
 *  @Author wonderful
 *  @Date 2020-5-22
 *  @Version 1.0
 *  @Description 相机接口
 */
public interface CameraInterface {

    /**
     * 拍照接口
     * @param bitmap 拍照转换后的bitmap
     * @param data 拍照原始数据
     */
    public void picture(Bitmap bitmap,byte[] data);
}
