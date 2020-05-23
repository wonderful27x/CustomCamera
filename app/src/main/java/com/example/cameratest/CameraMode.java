package com.example.cameratest;

import androidx.annotation.IntDef;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 相机模式，如拍照或扫码
 */
@IntDef({
        CameraMode.PICTURE,
        CameraMode.SCAN
})
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface CameraMode {
    int PICTURE = 0;  //拍照
    int SCAN = 1;     //扫码
}
