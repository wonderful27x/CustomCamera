package com.example.cameratest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
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

    private SurfaceHolder holder;//这是surfaceView提供的Holder
    private Activity activity;
    private Camera camera;
    private int cameraId;
    private int previewWidth;
    private int previewHeight;
    private int picWidth;
    private int picHeight;
    private String focusMode;
    private byte[] cameraBuff;
    private byte[] cameraBuffRotate;
    private int rotation;
    private Bitmap picBitmap;//拍照转换的bitmap

    private SizeChangedListener sizeChangedListener;
    //相机预览回掉，提供给外界使用
    private Camera.PreviewCallback previewCallback;

    public CameraHelper(Activity activity) {
        this.activity = activity;
        this.cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;             //默认启用后摄
        this.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE; //默认持续对焦
        this.previewWidth = -1;
        this.previewHeight = -1;
        this.picWidth = -1;
        this.picHeight = -1;
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
            //设置摄像头 图像传感器的角度、方向
            setPreviewOrientation(parameters);
            //设置对焦模式:持续对焦
            parameters.setFocusMode(focusMode);
            camera.setParameters(parameters);
            //数据缓存区,使用yuv数据格式计算大小
            cameraBuff = new byte[previewWidth * previewHeight * 3 / 2];
            cameraBuffRotate = new byte[previewWidth * previewHeight * 3 / 2];
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
     * @param parameters
     */
    private void setPreviewOrientation(Camera.Parameters parameters) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                //回掉
                if (sizeChangedListener != null) {
                    sizeChangedListener.onChanged(previewHeight, previewWidth);
                }
                break;
            case Surface.ROTATION_90: // 横屏 左边是头部(home键在右边)
                degrees = 90;
                //回掉
                if (sizeChangedListener != null) {
                    sizeChangedListener.onChanged(previewWidth, previewHeight);
                }
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                //回掉
                if (sizeChangedListener != null) {
                    sizeChangedListener.onChanged(previewHeight, previewWidth);
                }
                break;
            case Surface.ROTATION_270:// 横屏 头部在右边
                degrees = 270;
                //回掉
                if (sizeChangedListener != null) {
                    sizeChangedListener.onChanged(previewWidth, previewHeight);
                }
                break;
        }
        int result;
        //算法基本固定
        //如果是前摄
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        }
        //如果是后摄
        else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        //设置预览旋转角
        camera.setDisplayOrientation(result);
    }

    /**
     * Camera.PreviewCallback，相机预览回掉，
     * 当获取到预览数据时会回掉此方法，byte[] bytes是原始数据
     * 由于CameraHelper是相机封装类，当获取到预览数据是我们将其回掉出去，给外界使用
     * @param bytes
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (previewCallback != null) {
            previewCallback.onPreviewFrame(bytes,camera);
        }
        //这句代码很重要，去掉后回调回中断
        camera.addCallbackBuffer(cameraBuff);
    }


    /**
     * 拍照
     */
    public void takePicture(){
        //TODO 这个api的参数不太明白，待理解
        camera.takePicture(null, null,new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                byteToBitmap(data);
                PictureActivity.bitmap = picBitmap;
                Intent intent = new Intent(activity,PictureActivity.class);
                activity.startActivity(intent);
            }
        });
    }

    /**
     * 将byte类型的原始数据转换成bitmap
     */
    private void byteToBitmap(byte[] data){
        picBitmap = BitmapFactory.decodeByteArray(data,0,data.length);
        //前摄旋转270度
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){
            picBitmap = rotate(picBitmap,270.0f);
        }
        //后摄旋转90度
        else {
            picBitmap = rotate(picBitmap,90.0f);
        }
    }

    //水平镜像翻转
    private Bitmap mirror(Bitmap bitmap){
        Matrix matrix = new Matrix();
        matrix.postScale(-1f, 1f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
    //旋转
    private Bitmap rotate(Bitmap bitmap, Float degree){
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
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
        stopPreview(); //先停止预览
        startPreview();//再重新开始预览
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
}
