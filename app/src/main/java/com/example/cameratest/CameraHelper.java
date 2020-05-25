package com.example.cameratest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * 相机帮助类，主要用于摄像头数据的获取和预览
 * 相机有Camera和Camera2两套api，我们这里使用老版本的Camera
 * 关于相机的使用还是有些复杂的，其中涉及到很多基础概念，下面是推荐的几篇博客
 * https://www.jianshu.com/p/f8d0d1467584
 * https://www.jianshu.com/p/e20a2ad6ad9a
 */
public class CameraHelper implements Camera.PreviewCallback, SurfaceHolder.Callback {

    private static final String TAG = "CameraHelper";
    //这是surfaceView提供的Holder
    private SurfaceHolder holder;
    private Activity activity;
    private Camera camera;
    private int cameraId;
    //预览宽高
    private int previewWidth;
    private int previewHeight;
    //拍照宽高
    private int picWidth;
    private int picHeight;
    //聚焦模式
    private String focusMode;
    //预览数据缓存
    private byte[] cameraBuff;

    //拍照模式,默认为0
    //0：高质量-使用takePicture api
    //1：低质量-使用预览数据，这种模式下
    private int picLevel;
    //拍照标志，使用预览数据
    private boolean takingPicture;

    private SizeChangedListener sizeChangedListener;
    //相机预览回掉，提供给外界使用
    private Camera.PreviewCallback previewCallback;

    //相机数据运输接口，如拍照后将数据运输到外界
    private CameraDataTransport cameraDataTransport;

    public CameraHelper(Activity activity) {
        this.activity = activity;
        this.cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;             //默认启用后摄
        this.focusMode = null;                                            //使用默认对焦模式（待研究）
        this.previewWidth = -1;
        this.previewHeight = -1;
        this.picWidth = -1;
        this.picHeight = -1;
        this.picLevel = 0;
        this.takingPicture = false;
    }

    public void setHolder(SurfaceHolder holder) {
        if (this.holder != null) {
            this.holder.removeCallback(this);
            this.holder = null;
        }
        this.holder = holder;
        this.holder.addCallback(this);
    }

    public void switchCamera() {
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        stopPreview();
        startPreview();
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        try {
            //获得camera对象
            camera = Camera.open(cameraId);
            //设置camera属性
            Camera.Parameters parameters = camera.getParameters();
            //设置预览数据格式为NV21
            parameters.setPreviewFormat(ImageFormat.NV21);
            //设置摄像头预览尺寸
            setPreviewSize(parameters);
            //设置拍照后保存的图片的尺寸
            setPictureSize(parameters);
            //设置摄像头预览旋转角
            setPreviewOrientation();
            //设置对焦模式
            setCameraFocusMode(parameters,focusMode);
            camera.setParameters(parameters);
            //数据缓存区,使用yuv数据格式计算大小
            cameraBuff = new byte[previewWidth * previewHeight * 3 / 2];
            camera.addCallbackBuffer(cameraBuff);
            camera.setPreviewCallbackWithBuffer(this);
            //设置渲染点surfaceHolder
            camera.setPreviewDisplay(holder);
            //开启预览
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    /**
     * 设置相机对焦模式
     * @param parameters
     * @param mode
     */
    private void setCameraFocusMode(Camera.Parameters parameters,String mode){
        //如果mode为null不设置对焦模式，这时它的默认对焦模式是什么待研究
        if (mode != null){
            List<String> supportedFocusModes = parameters.getSupportedFocusModes();
            if (supportedFocusModes.contains(mode)){
                parameters.setFocusMode(mode);
            }
        }
    }


    /**
     * 获取一个最接近的尺寸，因为用户设置的尺寸相机不一定都支持，所以通过计算得到一个最接近的尺寸
     * @param expectWidth 期望的宽
     * @param expectHeight 期望的高
     * @param supportSize 相机支持的尺寸
     * @return 返回得到的最接近的尺寸
     */
    private Size getBestSize(int expectWidth, int expectHeight, List<Camera.Size> supportSize) {
        if (supportSize == null || supportSize.size() <=0){
            return null;
        }

        Camera.Size size = supportSize.get(0);
        Log.d(TAG, "Camera支持: " + size.width + "x" + size.height);

        //如果期望的宽高为-1则默认使用支持的第一个size
        if (expectWidth == -1 && expectHeight == -1){
            return new Size(size.width,size.height);
        }

        //选择一个与设置的差距最小的支持分辨率
        int m = Math.abs(size.width * size.height - expectWidth * expectHeight);
        supportSize.remove(0);
        Iterator<Camera.Size> iterator = supportSize.iterator();

        //遍历
        while (iterator.hasNext()) {
            Camera.Size next = iterator.next();
            Log.d(TAG, "支持 " + next.width + "x" + next.height);
            int n = Math.abs(next.height * next.width - expectWidth * expectHeight);
            if (n < m) {
                m = n;
                size = next;
            }
        }

        return new Size(size.width,size.height);
    }

    /**
     * 设置拍照保存的图片尺寸
     * @param parameters
     */
    private void setPictureSize(Camera.Parameters parameters){
        List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();
        Size bestSize = getBestSize(picWidth,picHeight,sizeList);
        if (bestSize != null){
            picWidth = bestSize.getWidth();
            picHeight = bestSize.getHeight();
            parameters.setPictureSize(picWidth,picHeight);
        }

        Log.d(TAG, "拍照分辨率 width:" + picWidth + " height:" + picHeight);
    }

    /**
     * 设置预览尺寸
     * @param parameters
     */
    private void setPreviewSize(Camera.Parameters parameters){
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        Size bestSize = getBestSize(previewWidth,previewHeight,sizeList);
        if (bestSize != null){
            previewWidth = bestSize.getWidth();
            previewHeight = bestSize.getHeight();
            parameters.setPreviewSize(previewWidth,previewHeight);
        }
        Log.d(TAG, "预览分辨率 width:" + previewWidth + " height:" + previewHeight);
    }

    /**
     * 设置预览旋转角度，由于传感器的方向是固定的，与屏幕的坐标又不吻合，需要做一些角度的旋转
     * 使得图像显示正常
     */
    private void setPreviewOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                //回掉
                if (sizeChangedListener != null) {
                    sizeChangedListener.onChanged(previewHeight, previewWidth);
                }
                break;
            case Surface.ROTATION_90: // 横屏 左边是头部(home键在右边)
                //回掉
                if (sizeChangedListener != null) {
                    sizeChangedListener.onChanged(previewWidth, previewHeight);
                }
                break;
            case Surface.ROTATION_180:
                //回掉
                if (sizeChangedListener != null) {
                    sizeChangedListener.onChanged(previewHeight, previewWidth);
                }
                break;
            case Surface.ROTATION_270:// 横屏 头部在右边
                //回掉
                if (sizeChangedListener != null) {
                    sizeChangedListener.onChanged(previewWidth, previewHeight);
                }
                break;
        }

        //需要考虑镜像,因为相机在预览的时候会做一个镜像的转换
        float realRotation = getRealRotationAngle(rotation,info,true);
        //设置预览旋转角
        camera.setDisplayOrientation((int) realRotation);
    }

    //根据屏幕旋转角和相机旋转角，得到图片实际需要旋转的角度，mirror是否考虑了镜像
    private float getRealRotationAngle(int screenRotation, Camera.CameraInfo cameraInfo,boolean mirror){
        int screenDegree = 0;
        switch (screenRotation) {
            case Surface.ROTATION_0:
                screenDegree = 0;
                break;
            case Surface.ROTATION_90: // 横屏 左边是头部(home键在右边)
                screenDegree = 90;
                break;
            case Surface.ROTATION_180:
                screenDegree = 180;
                break;
            case Surface.ROTATION_270:// 横屏 头部在右边
                screenDegree = 270;
                break;
        }
        Log.d(TAG, "屏幕旋转角： " + screenDegree);
        Log.d(TAG, "相机旋转角： " + cameraInfo.orientation);
        int result;
        //算法基本固定
        //如果是前摄
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + screenDegree) % 360;
            //镜像补偿
            if(mirror){
                result = (360 - result) % 360;
            }
        }
        //如果是后摄
        else {
            result = (cameraInfo.orientation - screenDegree + 360) % 360;
        }
        Log.d(TAG, "实际旋转角： " + result);
        return result;
    }

    /**
     * Camera.PreviewCallback，相机预览回掉，
     * 当获取到预览数据时会回掉此方法，byte[] bytes是原始数据
     * 由于CameraHelper是相机封装类，当获取到预览数据是我们将其回掉出去，给外界使用
     * @param bytes
     * @param camera
     */
    @Override
    public void onPreviewFrame(final byte[] bytes, Camera camera) {
        if (previewCallback != null) {
            previewCallback.onPreviewFrame(bytes,camera);
        }
        //这句代码很重要，去掉后回调回中断
        camera.addCallbackBuffer(cameraBuff);

        //如果是picLevel == 1使用预览数据作为拍照数据
        if(picLevel == 1 && takingPicture){
            takingPicture = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Handler handler = new Handler(activity.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bitmap = transform(bytes,1);
                            if (cameraDataTransport != null) {
                                cameraDataTransport.picture(bitmap, bytes);
                            }
                        }
                    });
                }
            }).start();
        }
    }

    /**
     * 拍照
     */
    public void takePicture(){
        //如果picLevel == 0使用takePicture api拍照
        if (picLevel == 0){
            //TODO 这个api的参数不太明白，待理解
            new Thread(new Runnable() {
                @Override
                public void run() {
                    camera.takePicture(null, null,new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(final byte[] data, Camera camera) {
                            final Bitmap bitmap = transform(data,0);
                            if (cameraDataTransport != null){
                                Handler handler = new Handler(activity.getMainLooper());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        cameraDataTransport.picture(bitmap,data);
                                    }
                                });
                            }
                        }
                    });
                }
            }).start();
        }
        //如果picLevel == 1，takingPicture置为true，在一帧预览数据到来时作为拍照数据
        else if (picLevel == 1){
            this.takingPicture = true;
        }
    }

    //byte数据转成bitmap，旋转后
    private Bitmap transform(byte[] data,int level){
        Bitmap bitmap = null;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        //设备旋转角，手机和pad不一样
        int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        //计算得到需要旋转的角度,无需考虑镜像，因为data是没有做镜像转换的
        float rotation = getRealRotationAngle(displayRotation,info,false);
        //如果是takePicture api数据，这是图片格式（JPEG）
        if (level == 0){
            //转成bitmap
            bitmap = bytToBitmap(data);
            //旋转
            bitmap = rotate(bitmap,rotation);
        }
        //如果使用预览数据,这是YUV格式
        else {
            //旋转并转成bitmap
            //其实这里也可以先转成bitmap在旋转
            if (rotation == 90){
                byte[] transformData = rotateYUV420Degree90(data,previewWidth,previewHeight);
                bitmap = nv21ToBitmap(transformData,previewHeight,previewWidth);
            }else if(rotation == 180){
                byte[] transformData = rotateYUV420Degree180(data,previewWidth,previewHeight);
                bitmap = nv21ToBitmap(transformData,previewWidth,previewHeight);
            }else if(rotation == 270){
                byte[] transformData = rotateYUV420Degree270(data,previewWidth,previewHeight);
                bitmap = nv21ToBitmap(transformData,previewHeight,previewWidth);
            }
        }
        //如果是前摄，还需要镜像翻转
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
            bitmap = mirror(bitmap);
        }
        return bitmap;
    }

    //重新开始预览
    public void rePreview(){
        stopPreview(); //先停止预览
        startPreview();//再重新开始预览
    }

    //////////////////////////////////////////SurfaceHolder.Callback////////////////////////////////
    //我们这里使用SurfaceView来进行预览，SurfaceHolder.Callback用于监听Surface的变化，我们实现这个接口以便做相应处理
    //实现的接口需要设置给SurfaceHolder，这个SurfaceHolder就是SurfaceView所提供的
    /**
     * surface创建时回调
     * @param surfaceHolder
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
    }

    /**
     * surface改变时回调，如横竖屏切换
     * @param surfaceHolder
     * @param i
     * @param i1
     * @param i2
     */
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        rePreview();
    }

    /**
     * surface销毁是回调
     * @param surfaceHolder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //做释放资源处理
        stopPreview();
    }

    /**
     * surface大小发生改变时会重新计算预览宽高，将这个信息回掉出去
     */
    public interface SizeChangedListener {
        public void onChanged(int width, int height);
    }

    public void setSizeChangedListener(SizeChangedListener sizeChangedListener) {
        this.sizeChangedListener = sizeChangedListener;
    }


    public int getPreviewWidth() {
        return previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public int getPicWidth() {
        return picWidth;
    }

    public int getPicHeight() {
        return picHeight;
    }

    public String getFocusMode() {
        return focusMode;
    }

    public void setFocusMode(String focusMode) {
        this.focusMode = focusMode;
    }

    public int getCameraId() {
        return cameraId;
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    public int getPicLevel() {
        return picLevel;
    }

    public void setPicLevel(int picLevel) {
        this.picLevel = picLevel;
    }

    /**
     * 设置预览尺寸
     * @param expectWidth
     * @param expectHeight
     * @return
     */
    public void setExpectPreviewSize(int expectWidth, int expectHeight){
        this.previewWidth = expectWidth;
        this.previewHeight = expectHeight;
    }

    /**
     * 设置拍照尺寸
     * @param expectWidth
     * @param expectHeight
     * @return
     */
    public void setExpectPicSize(int expectWidth, int expectHeight){
        this.picWidth = expectWidth;
        this.picHeight = expectHeight;
    }

    public void setCameraDataTransport(CameraDataTransport cameraDataTransport) {
        this.cameraDataTransport = cameraDataTransport;
    }

    //数组转bitmap，注意这里的数组data并不是yuv，而是图片格式（jpeg）
    private Bitmap bytToBitmap(byte[] data){
        return BitmapFactory.decodeByteArray(data,0,data.length);
    }


    /**
     * 将bitmap转为数组
     * @param bitmap 图片
     * @return 返回数组
     */
    public byte[] bitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bitmap.getByteCount());
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return outputStream.toByteArray();
    }

    //水平镜像翻转
    private Bitmap mirror(Bitmap bitmap){
        Matrix matrix = new Matrix();
        matrix.postScale(-1f, 1f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    //旋转
    private Bitmap rotate(Bitmap bitmap, float degree){
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    //将nv21原始数据转成bitmap
    private Bitmap nv21ToBitmap(byte[] nv21, int width, int height) {
        Bitmap bitmap = null;
        try {
            YuvImage image = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, width, height), 80, stream);
            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    //旋转90度以得到正常显示图片
    private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth)
                        + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    //旋转180度以得到正常显示图片
    private byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int i = 0;
        int count = 0;
        for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
            yuv[count] = data[i];
            count++;
        }
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth
                * imageHeight; i -= 2) {
            yuv[count++] = data[i - 1];
            yuv[count++] = data[i];
        }
        return yuv;
    }

    //旋转270度以得到正常显示图片
    private byte[] rotateYUV420Degree270(byte[] data, int imageWidth,
                                               int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int nWidth = 0, nHeight = 0;
        int wh = 0;
        int uvHeight = 0;
        if (imageWidth != nWidth || imageHeight != nHeight) {
            nWidth = imageWidth;
            nHeight = imageHeight;
            wh = imageWidth * imageHeight;
            uvHeight = imageHeight >> 1;// uvHeight = height / 2
        }

        int k = 0;
        for (int i = 0; i < imageWidth; i++) {
            int nPos = 0;
            for (int j = 0; j < imageHeight; j++) {
                yuv[k] = data[nPos + i];
                k++;
                nPos += imageWidth;
            }
        }
        for (int i = 0; i < imageWidth; i += 2) {
            int nPos = wh;
            for (int j = 0; j < uvHeight; j++) {
                yuv[k] = data[nPos + i];
                yuv[k + 1] = data[nPos + i + 1];
                k += 2;
                nPos += imageWidth;
            }
        }
        return rotateYUV420Degree180(rotateYUV420Degree90(data, imageWidth, imageHeight), imageWidth, imageHeight);
    }
}
