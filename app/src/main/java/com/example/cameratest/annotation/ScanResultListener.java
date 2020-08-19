package com.example.cameratest.annotation;

import com.example.cameratest.core.ScanResult;

/**
 *  @Author wonderful
 *  @Date 2020-8-18
 *  @Version 1.0
 *  @Description 二维码/条形码扫描结果返回接口
 */
public interface ScanResultListener {
    public void onScanResult(ScanResult scanResult, byte[] scanBitmap);
}
