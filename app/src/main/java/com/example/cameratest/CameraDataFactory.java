package com.example.cameratest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 *  @Author wonderful
 *  @Date 2020-5-22
 *  @Version 1.0
 *  @Description 相机数据处理模块-相机数据加工厂，专门处理相机的数据，如水印、扫码、图像识别等
 */
public class CameraDataFactory {

    public Bitmap byteToBitmap(byte[] data){
        Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
        return bitmap;
    }

}
