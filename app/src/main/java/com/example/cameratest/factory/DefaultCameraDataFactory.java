package com.example.cameratest.factory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.cameratest.annotation.CameraDataFactory;

/**
 *  @Author wonderful
 *  @Date 2020-5-23
 *  @Version 1.0
 *  @Description 默认的相机数据加工厂
 */
public class DefaultCameraDataFactory implements CameraDataFactory {

    @Override
    public Bitmap working(Bitmap bitmap, byte[] data) {
        if (bitmap != null){
            return bitmap;
        }
        return BitmapFactory.decodeByteArray(data,0,data.length);
    }
}
