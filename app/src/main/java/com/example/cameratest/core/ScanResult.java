package com.example.cameratest.core;

import java.io.Serializable;

/**
 *  @Author wonderful
 *  @Date 2020-8-19
 *  @Version 1.0
 *  @Description 二维码扫描结果
 */
public class ScanResult implements Serializable {
    public String format;    //格式，如：QR_CODE
    public String type;      //类型，如：url
    public String dateTime;  //时间
    public String metadata;  //元数据
    public String content;   //二维码结果
}
