package com.example.min.jvideoplay.media;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.example.min.jvideoplay.R;


public class StarMediaController extends BaseMediaController {
    private static final String TAG = "zl";
    private static final int SHOW_PLAY_PROGRESS = 2;

    private Context mContext;

    private View mRoot;
    private ViewGroup mAnchor;
//    private CircleProgressBar mScheduleProgress;// 进度条

    private float mProgress;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_PLAY_PROGRESS:
                    updatePlayProgress();
                    break;
                default:
                    break;
            }
        }
    };

    public StarMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public StarMediaController(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        if (mRoot != null) {
            initControllerView(mRoot);
        }
    }

    @Override
    protected void setPlayerListeners() {
        // TODO Auto-generated method stub
        super.setPlayerListeners();
    }

    /**
     * 设置MediaController 所依附的的父视图
     */
    @Override
    protected void setAnchorView(ViewGroup view) {
        // TODO Auto-generated method stub
        mAnchor = view;
        mRoot = getControllerView();
        initControllerView(mRoot);
        show();
    }

    /**
     * 获取依附的父视图
     *
     * @return
     */
    public View getAnchorView() {
        return mAnchor;
    }

    /**
     * 默认控制器视图
     *
     * @return
     */
    protected View makeControllerView() {
        return null;//LayoutInflater.from(mContext).inflate(R.layout.player_control_vod_star_fullscreen, this);
    }

    private void initControllerView(View rootView) {
//        mScheduleProgress = (CircleProgressBar) rootView.findViewById(R.id.cpb_progress);
//        mScheduleProgress.setMax(1000);
    }

    @Override
    public void setFileName(String name) {
    }

    @Override
    public boolean isShowing() {
        return true;
    }

    /**
     * 显示控制器，并判断是否发送消息更新进度条
     */
    @Override
    public void show() {
//        if (mAnchor != null) {
//            ViewParent rootParent = mRoot.getParent();
//            if (rootParent == null) {
//                ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//                mAnchor.addView(mRoot, tlp);
//            }
//        }
//        mHandler.removeMessages(SHOW_PLAY_PROGRESS);
//        mHandler.sendEmptyMessage(SHOW_PLAY_PROGRESS);
//        if (mProgress == 1000 && mOnVideoPlayListener != null) {
//            mOnVideoPlayListener.onVideoPlayComplete();
//        }

    }

    @Override
    protected void show(int timeout) {
    }

    /**
     * 隐藏控制器
     */
    public void hide() {

    }

    public void stopUpdatePlayProgress() {
        mHandler.removeMessages(SHOW_PLAY_PROGRESS);
    }

    /**
     * 更新播放进度
     */
    private void updatePlayProgress() {
//        if (mPlayer == null) {
//            return;
//        }
//        long duration = mPlayer.getDuration();
//        long position = mPlayer.getCurrentPosition();
//
//        if (position != 0 && !mIsVideoPlay && mOnVideoPlayListener != null) {
//            mIsVideoPlay = true;
//            Log.i(TAG, "StarMediaController updatePlayProgress... mOnVideoPlayListener.onVideoPlay()");
//            mOnVideoPlayListener.onVideoPlay();
//        }
//        if (mScheduleProgress != null && duration > 0) {
//            float pos = (1000L * position / duration);
//            mProgress = pos;
//            mScheduleProgress.setProgress(pos);
//            long delayMillis = duration > 20 * 1000 ? 1000 : 500;//如果视频大于20s,1s更新一次进度,否则500ms就更新一次进度
//            mHandler.sendEmptyMessageDelayed(SHOW_PLAY_PROGRESS, delayMillis);
//        }

    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getRepeatCount() == 0
                && (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE)) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }


    public View getControllerView() {
        if (mRoot == null) {
            mRoot = makeControllerView();
        }
        return mRoot;
    }

    public void toggleButtons(boolean isFullScreen) {
//        mIsFullScreen = isFullScreen;
//        updateScaleButton();
//        updateBackButton();
    }
}
