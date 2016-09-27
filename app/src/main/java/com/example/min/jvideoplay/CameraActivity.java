package com.example.min.jvideoplay;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.min.jvideoplay.camera.CameraTextureView;
import com.example.min.jvideoplay.camera.configuration.uitl.CLog;
import com.example.min.jvideoplay.utils.DateUtil;
import com.example.min.jvideoplay.utils.FileUtils;
import com.example.min.jvideoplay.video.VideoPlayActivity;
import com.example.min.jvideoplay.view.CircleProgressBar;

public class CameraActivity extends AppCompatActivity {

    private static String TAG=CameraActivity.class.getSimpleName();

    private CameraTextureView mCameraTextureView;
    private CircleProgressBar mCirclePb;
    private ImageView mIvRecordSwitch;
    private boolean mIsRecording = false;
    private String recoderPath;
    private long mActionDownTime;
    private static final int DURATION_TIME = 1000;
    private static final int DURATION_MAXTIME = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        initView();
    }

    private void initView(){
        mCameraTextureView= (CameraTextureView) findViewById(R.id.gl_recorder_surface_view);
        mCirclePb= (CircleProgressBar) findViewById(R.id.cpb_record_progress);
        mIvRecordSwitch= (ImageView) findViewById(R.id.iv_record_switch);
        mCirclePb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        mActionDownTime=System.currentTimeMillis();
                        startRecoder();
                        break;
                    case MotionEvent.ACTION_UP:
                        long time=System.currentTimeMillis()-mActionDownTime;
                        if(time<DURATION_TIME){
                            stopRecoder(false);
                        }else if(time<DURATION_MAXTIME){
                            stopRecoder(true);
                        }
                        break;
                }
                return true;
            }
        });

        mCirclePb.setmProgressBarListener(new CircleProgressBar.ProgressBarListener() {
            @Override
            public void stopProgressLinstener() {
                stopRecoder(true);
            }
        });
        mIvRecordSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCameraTextureView.switchCamera();
            }
        });
    }
    //开始录制
    private void startRecoder(){
        String recoderName= DateUtil.getTimeString(System.currentTimeMillis(),DateUtil.DATE_TYPEYMDHMS)+".mp4";
        recoderPath= FileUtils.getRecorderDirectory(this)+"/"+recoderName;
        mCameraTextureView.startRecording(recoderPath, new CameraTextureView.OnRecordingStartCallback() {
            @Override
            public void onRecordingStart(boolean success) {
                mIsRecording=success;
                Log.i(TAG,"是否成功"+mIsRecording);
                if (success) {
                    CLog.d(TAG, "启动录制成功");
                    mCirclePb.startProgressWithAnimation(100, 10 * 1000);
                } else {
                    CLog.d(TAG, "启动录制失败");
                }
            }
        });
    }
    //停止录制
    private void stopRecoder(final boolean recordingSuccess){
        if(!mIsRecording){
            return;
        }
        mCirclePb.stopProgressWidthAnimation();
        mCirclePb.setProgress(0, true);
        mCameraTextureView.stopRecording(new CameraTextureView.OnRecordingEndCallback() {
            @Override
            public void onRecordingEnd(boolean success) {
                CLog.d(TAG, mIsRecording+"停止录制,state: " + success);
                if (recordingSuccess) {
                    mIsRecording = false;
                    finishCompleted();
                } else {
                    FileUtils.deleteFile(recoderPath);
                }
            }
        });
    }

    private static final int MSG_RECORDING_START = 1;
    private static final int MSG_RECORDING_END = 2;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_RECORDING_START:
                    mCirclePb.startProgressWithAnimation(100, 10 * 1000);
                    break;
                case MSG_RECORDING_END:
                    finishCompleted();
                    break;
            }
        }
    };

    private void finishCompleted() {
        mCameraTextureView.cameraInstance().stopCamera();//释放摄像头
        mCirclePb.setProgress(0, true);
        Intent mIntent = new Intent(this,VideoPlayActivity.class);
        mIntent.putExtra(VideoPlayActivity.VIDEOPATH, recoderPath);
        startActivity(mIntent);
    }
}
