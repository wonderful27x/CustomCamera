package com.example.cameratest;

import androidx.annotation.IntDef;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 拍照类型注解
 */
@IntDef({
        PicType.PIC_DEFAULT,
        PicType.PIC_WATER_MARK
})
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface PicType {
    int PIC_DEFAULT = 0;     //普通拍照
    int PIC_WATER_MARK = 1;  //水印
}
