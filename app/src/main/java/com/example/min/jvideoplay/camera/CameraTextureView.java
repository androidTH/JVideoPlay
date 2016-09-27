package com.example.min.jvideoplay.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.TextureView;
import android.view.WindowManager;

import com.example.min.jvideoplay.camera.configuration.CaptureConfiguration;
import com.example.min.jvideoplay.camera.configuration.PredefinedCaptureConfigurations;
import com.example.min.jvideoplay.camera.configuration.uitl.CameraInstance;
import com.example.min.jvideoplay.camera.configuration.uitl.CameraRecorder;


public class CameraTextureView extends TextureView implements TextureView.SurfaceTextureListener {
    private static final String TAG = "zl";
    private SurfaceTexture mSurfaceTexture;

    //protected int mRecordWidth = 720;
    //protected int mRecordHeight = 1280;
    public int maxPreviewWidth = 1280;
    public int maxPreviewHeight = 1280;
    public int mSurfacewidth;
    private CaptureConfiguration mCaptureConfiguration;

    //是否使用后置摄像头
    protected boolean mIsCameraBackForward = true;

    private CameraRecorder mCameraRecorder;

    public interface OnRecordingStartCallback {
        void onRecordingStart(boolean success);
    }

    public interface OnRecordingEndCallback {
        void onRecordingEnd(boolean success);
    }

    public CameraTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCaptureConfiguration = createCaptureConfiguration();
        setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureAvailable...");
        mSurfaceTexture = surface;
        this.mSurfacewidth = width;

        if (!cameraInstance().isCameraOpened()) {
            Log.i(TAG,mCaptureConfiguration.getVideoWidth()+"宽"+mCaptureConfiguration.getVideoHeight()+"高");
            setRecordingSize(mCaptureConfiguration.getVideoWidth(), mCaptureConfiguration.getVideoHeight());
//            setRecordingSize(mCaptureConfiguration.getVideoHeight(), mCaptureConfiguration.getVideoWidth());
            if (!cameraInstance().tryOpenCamera(null, getFacing())) {
                Log.e(TAG, "相机启动失败!!");
            }
        }

        if (!cameraInstance().isPreviewing()) {
            cameraInstance().startPreview(surface);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.i(TAG, "onSurfaceTextureDestroyed...");
        cameraInstance().stopCamera();
        if (mCameraRecorder != null) {
            mCameraRecorder.releaseRecorderResources();
            mCameraRecorder = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureSizeChanged...");
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//        Log.i(TAG, "onSurfaceTextureUpdated...");

    }

    public void startRecording(final String recordPath, final OnRecordingStartCallback callback) {
        post(new Runnable() {
            @Override
            public void run() {
                if (mCameraRecorder == null) {
                    mCameraRecorder = new CameraRecorder(recordPath, cameraInstance(), createCaptureConfiguration());
                }
                boolean recordingState = mCameraRecorder.toggleRecording();
                if (callback != null) {
                    callback.onRecordingStart(recordingState);
                }
            }
        });
    }

    public void stopRecording(final OnRecordingEndCallback callback) {
        post(new Runnable() {
            @Override
            public void run() {
                boolean recordingState = mCameraRecorder.toggleRecording();
                if (callback != null) {
                    callback.onRecordingEnd(recordingState);
                }
            }
        });
    }

    public CameraInstance cameraInstance() {
        return CameraInstance.getInstance();
    }

    private int getFacing() {
        return mIsCameraBackForward ? CameraInstance.CAMERA_BACK : CameraInstance.CAMERA_FRONT;
    }

    //在onSurfaceCreated之前设置有效
    public void presetCameraForward(boolean isBackForward) {
        mIsCameraBackForward = isBackForward;
    }

    /**
     * 注意，录制的尺寸将影响preview的尺寸
     * 这里的width和height表示竖屏尺寸
     * 在onSurfaceCreated之前设置有效
     *
     * @param width
     * @param height
     */
    private void setRecordingSize(int width, int height) {
        if (width > maxPreviewWidth || height > maxPreviewHeight) {
            float scaling = Math.min(maxPreviewWidth / (float) width, maxPreviewHeight / (float) height);
            width = (int) (width * scaling);
            height = (int) (height * scaling);
        }
        cameraInstance().setPreferPreviewSize(width, height);
        int rotationCorrection = getRotationCorrection();
        cameraInstance().setDisplayOrientation(rotationCorrection);
        Log.i(TAG, String.format("presetRecordingSize,%d x %d, rotationCorrection,%d", width, height, rotationCorrection));
    }


    private int getRotationCorrection() {
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int displayRotation = display.getRotation() * 90;
        return (cameraInstance().getCameraOrientation(getFacing()) - displayRotation + (mIsCameraBackForward ? 360 : 180)) % 360;
    }

    private void setCameraScale(){
        Matrix matrix = new Matrix();
        if(!mIsCameraBackForward){
            matrix.setScale(-1, 1);
            matrix.postTranslate(mSurfacewidth, 0);
            setTransform(matrix);
        }
    }

    public synchronized void switchCamera() {
        mIsCameraBackForward = !mIsCameraBackForward;
        post(new Runnable() {
            @Override
            public void run() {
                cameraInstance().stopCamera();
                cameraInstance().tryOpenCamera(new CameraInstance.CameraOpenCallback() {
                    @Override
                    public void cameraReady() {
                        if (!cameraInstance().isPreviewing()) {
                            cameraInstance().setDisplayOrientation(getRotationCorrection());
//                            setCameraScale();
                            Log.i(TAG, "## switch camera -- start preview...");
                            cameraInstance().startPreview(mSurfaceTexture);
                        }
                    }
                }, getFacing());

            }
        });
    }

    public synchronized void resumePreview() {
        if (!cameraInstance().isCameraOpened()) {
            cameraInstance().tryOpenCamera(new CameraInstance.CameraOpenCallback() {
                @Override
                public void cameraReady() {
                    Log.i(TAG, "tryOpenCamera OK...");
                }
            }, getFacing());
        }
        if (!cameraInstance().isPreviewing()) {
            cameraInstance().startPreview(mSurfaceTexture);
        }
    }

    /**
     * 注意,focusAtPoint 会强制 focus mode 为 FOCUS_MODE_AUTO
     * 如果有自定义的focus mode， 请在 AutoFocusCallback 里面重设成所需的focus mode。
     * x,y 取值范围: [0, 1]， 一般为 touchEventPosition / viewSize.
     *
     * @param x
     * @param y
     * @param focusCallback
     */
    public void focusAtPoint(float x, float y, Camera.AutoFocusCallback focusCallback) {
        cameraInstance().focusAtPoint(y, 1.0f - x, focusCallback);
    }

    /**
     * @param mode 参数为
     *             Camera.Parameters.FLASH_MODE_AUTO;
     *             Camera.Parameters.FLASH_MODE_OFF;
     *             Camera.Parameters.FLASH_MODE_ON;
     *             Camera.Parameters.FLASH_MODE_RED_EYE
     *             Camera.Parameters.FLASH_MODE_TORCH 等
     * @return
     */
    public synchronized boolean setFlashLightMode(String mode) {
        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Log.e(TAG, "当前设备不支持闪光灯!");
            return false;
        }
        if (!mIsCameraBackForward) {
            return false;
        }
        Camera.Parameters parameters = cameraInstance().getParams();
        if (parameters == null) {
            return false;
        }
        try {
            if (!parameters.getSupportedFlashModes().contains(mode)) {
                Log.e(TAG, "Invalid Flash Light Mode!!!");
                return false;
            }
            parameters.setFlashMode(mode);
            cameraInstance().setParams(parameters);
        } catch (Exception e) {
            Log.e(TAG, "修改闪光灯状态失败, 请检查是否正在使用前置摄像头?");
            return false;
        }
        return true;
    }

    public void stopPreview() {
        post(new Runnable() {
            @Override
            public void run() {
                cameraInstance().stopPreview();
            }
        });
    }

    private CaptureConfiguration createCaptureConfiguration() {
        final PredefinedCaptureConfigurations.CaptureResolution resolution = getResolution(1);
        final PredefinedCaptureConfigurations.CaptureQuality quality = getQuality(2);
        int fileDuration = CaptureConfiguration.NO_DURATION_LIMIT;
        int fileSize = CaptureConfiguration.NO_FILESIZE_LIMIT;
        boolean showTimer = false;
        final CaptureConfiguration config = new CaptureConfiguration(resolution, quality, fileDuration, fileSize, showTimer);
        return config;
    }

    private PredefinedCaptureConfigurations.CaptureQuality getQuality(int position) {
        final PredefinedCaptureConfigurations.CaptureQuality[] quality = new PredefinedCaptureConfigurations.CaptureQuality[]
                {PredefinedCaptureConfigurations.CaptureQuality.HIGH,
                        PredefinedCaptureConfigurations.CaptureQuality.MEDIUM,
                        PredefinedCaptureConfigurations.CaptureQuality.LOW};
        return quality[position];
    }

    private PredefinedCaptureConfigurations.CaptureResolution getResolution(int position) {
        final PredefinedCaptureConfigurations.CaptureResolution[] resolution = new PredefinedCaptureConfigurations.CaptureResolution[]
                {PredefinedCaptureConfigurations.CaptureResolution.RES_1080P,
                        PredefinedCaptureConfigurations.CaptureResolution.RES_720P,
                        PredefinedCaptureConfigurations.CaptureResolution.RES_480P};
        return resolution[position];
    }

}
