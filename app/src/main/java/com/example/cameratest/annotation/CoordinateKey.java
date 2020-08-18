package com.example.cameratest.annotation;

/**
 *  @Author wonderful
 *  @Date 2020-8-18
 *  @Version 1.0
 *  @Description 相机模式枚举器
 */
public enum CoordinateKey {

    BACK_BTN("back_btn",0),              //返回按钮
    ALBUM_BTN("album_btn",1),            //相册选择按钮
    SWITCH_BTN("switch_btn",2);          //前后摄像头切换按钮


    private String key;
    private int code;

    CoordinateKey(String key, int code) {
        this.key = key;
        this.code = code;
    }

    public String getKey() {
        return key;
    }

    public int getCode() {
        return code;
    }

    public static CoordinateKey coordinateKey(String key){
        for (CoordinateKey coordinateKey:values()){
            if (coordinateKey.getKey().equals(key)){
                return coordinateKey;
            }
        }
        return null;
    }
}
