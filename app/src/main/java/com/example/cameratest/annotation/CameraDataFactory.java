package com.example.cameratest.annotation;

import android.graphics.Bitmap;

/**
 *  @Author wonderful
 *  @Date 2020-5-23
 *  @Version 1.0
 *  @Description 相机数据处理模块-相机数据加工厂，专门处理相机的数据，如水印等
 */
public interface CameraDataFactory{
    /**
     * 需要实现的加工方法
     * @param bitmap 原材料
     * @param data 原材料
     * @return 加工后的bitmap
     */
    public Bitmap working(Bitmap bitmap,byte[] data);
}
