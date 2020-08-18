package com.example.cameratest;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *  @Author wonderful
 *  @Date 2020-8-18
 *  @Version 1.0
 *  @Description 二维码扫描管理类
 */
public class ScanManager implements Camera.PreviewCallback {

    private static final String TAG = "ScanManager";

    private static final int READY = 0;
    private static final int DECODING = 1;
    private static final int CLOSE = 2;

    private Point scanSize;                      //扫描区域大小
    private Point previewSize;                   //预览大小
    private Point screenSize;                    //屏幕大小
    private int state;                           //状态 0:就绪 1：正在解码

    //二维码扫描框
    private Rect framingRectInPreview;
    //解码器
    private  MultiFormatReader multiFormatReader;
    //扫描结果返回接口
    private ScanResultListener scanResultListener;
    //线程池
    private ExecutorService executorService;
    //Handler
    private Handler handler;

    public ScanManager(Point scanSize, Point previewSize, Point screenSize) {
        this.scanSize = scanSize;
        this.previewSize = previewSize;
        this.screenSize = screenSize;
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(null);
        executorService = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * 相机预览数据回调，
     * @param data yuv原始数据
     * @param camera 相机
     */
    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        if (state == READY){
            state = DECODING;
            if (executorService == null) return;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    decode(data,previewSize.x,previewSize.y);
                }
            });
        }
    }

    /**
     * TODO imitate from google zxing
     *
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        long start = System.nanoTime();
        Result rawResult = null;
        PlanarYUVLuminanceSource source = buildLuminanceSource(data, width, height);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                if (multiFormatReader == null)return;
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                if (multiFormatReader != null){
                    multiFormatReader.reset();
                }
            }
        }

        //返回结果
        if (rawResult != null){
            long end = System.nanoTime();
            Log.d(TAG, "Found barcode in " + TimeUnit.NANOSECONDS.toMillis(end - start) + " ms");
            final byte[] scanData = sourceToByteArray(source);
            if (scanResultListener != null){
                final Result finalRawResult = rawResult;
                if (handler == null)return;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (scanResultListener != null){
                            scanResultListener.onScanResult(finalRawResult,scanData);
                        }
                    }
                });
            }
        }else {
            reset();
        }
    }

    /**
     * TODO copy from google zxing
     *
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data A preview frame.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    private PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = getFramingRectInPreview();
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                rect.width(), rect.height(), false);
    }

    /**
     * TODO imitate from google zxing
     *
     * Like {@link #getFramingRect} but coordinates are in terms of the preview frame,
     * not UI / screen.
     *
     * @return {@link Rect} expressing barcode scan area in terms of the preview size
     */
    private Rect getFramingRectInPreview() {
        if (framingRectInPreview == null) {
            Rect framingRect = getFramingRect();
            if (framingRect == null) {
                return null;
            }
            Rect rect = new Rect(framingRect);
            Point cameraResolution = previewSize;
            Point screenResolution = screenSize;
            if (cameraResolution == null || screenResolution == null) {
                // Called early, before init even finished
                return null;
            }
            rect.left = rect.left * cameraResolution.x / screenResolution.x;
            rect.right = rect.right * cameraResolution.x / screenResolution.x;
            rect.top = rect.top * cameraResolution.y / screenResolution.y;
            rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
            framingRectInPreview = rect;
        }
        return framingRectInPreview;
    }

    /**
     * TODO imitate from google zxing
     *
     * Calculates the framing rect which the UI should draw to show the user where to place the
     * barcode. This target helps with alignment as well as forces the user to hold the device
     * far enough away to ensure the image will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    private Rect getFramingRect() {
        Point screenResolution = screenSize;
        if (screenResolution == null) {
            // Called early, before init even finished
            return null;
        }

        int width = scanSize.x;
        int height = scanSize.y;

        int leftOffset = (screenResolution.x - width) / 2;
        int topOffset = (screenResolution.y - height) / 2;
        Rect framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
        Log.d(TAG, "Calculated framing rect: " + framingRect);
        return framingRect;
    }

    //将扫描框内容转成byte数组
    private byte[] sourceToByteArray(PlanarYUVLuminanceSource source) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        return out.toByteArray();
    }

    public void setScanResultListener(ScanResultListener scanResultListener) {
        this.scanResultListener = scanResultListener;
    }

    public void reset(){
        if (state == CLOSE)return;
        state = READY;
    }

    public void release(){
        //扫描结果返回接口
        scanResultListener = null;
        //解码器
        multiFormatReader = null;
        //handler
        handler = null;
        //线程池
        if(executorService != null){
            executorService.shutdownNow();
            executorService = null;
        }
    }
}
