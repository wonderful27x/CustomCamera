package com.example.cameratest;

import android.graphics.Bitmap;

/**
 *  @Author wonderful
 *  @Date 2020-5-23
 *  @Version 1.0
 *  @Description 相机数据处理模块-相机数据加工厂，专门处理相机的数据，如水印、扫码、图像识别等
 */
public interface CameraDataFactory{

    //普通拍照功能，将原始数据转换成bitmap,如果已经转换了直接返回
    public Bitmap picture(Bitmap bitmap,byte[] data);

    //水印加工
    public Bitmap pictureWatermark(Bitmap bitmap,String mark);
}
