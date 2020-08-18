package com.example.cameratest;

/**
 *  @Author wonderful
 *  @Date 2020-5-23
 *  @Version 1.0
 *  @Description 相机模式枚举器
 */
public enum CameraMode {

    PICTURE(Keys.PICTURE,0),        //拍照
    SCAN(Keys.SCAN,1),              //扫码
    RECOGNIZE(Keys.RECOGNIZE,2);    //识别

    private String mode;
    private int code;

    CameraMode(String mode, int code) {
        this.mode = mode;
        this.code = code;
    }

    public String getMode() {
        return mode;
    }

    public int getCode() {
        return code;
    }

    public static CameraMode cameraMode(String mode){
        for (CameraMode mode1:values()){
            if (mode1.getMode().equals(mode)){
                return mode1;
            }
        }
        return null;
    }
}
