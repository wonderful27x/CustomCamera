package com.example.cameratest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *  @Author wonderful
 *  @Date 2020-5-23
 *  @Version 1.0
 *  @Description 默认的相机数据加工厂
 */
public class DefaultCameraDataFactory implements CameraDataFactory{

    @Override
    public Bitmap picture(Bitmap bitmap,byte[] data) {
        if (bitmap != null){
            return bitmap;
        }
        return BitmapFactory.decodeByteArray(data,0,data.length);
    }

    @Override
    public Bitmap pictureWatermark(Bitmap bitmap, String mark) {
        //TODO
        return null;
    }
}
