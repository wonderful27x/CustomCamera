package com.example.cameratest.factory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.example.cameratest.annotation.CameraDataFactory;

/**
 *  @Author wonderful
 *  @Date 2020-8-18
 *  @Version 1.0
 *  @Description 旋转工厂
 */
public class RotationFactory implements CameraDataFactory {

    @Override
    public Bitmap working(Bitmap source, byte[] data) {
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Matrix matrix = new Matrix();
        matrix.postRotate(180);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        return bitmap;
    }
}
