package com.example.min.jvideoplay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;


import com.example.min.jvideoplay.media.BaseMediaController;
import com.example.min.jvideoplay.media.Const;
import com.example.min.jvideoplay.media.IRenderView;
import com.example.min.jvideoplay.media.JVideoView;
import com.example.min.jvideoplay.media.StarMediaController;
import com.example.min.jvideoplay.utils.RuleUtils;

import java.lang.reflect.Field;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by min on 2016/7/22.
 */
public class MediaPlayVFragment extends Fragment {
    private static final String TAG = MediaPlayVFragment.class.getSimpleName();
    /**
     * 点播视频当前播放的位置
     */
    public long currentPosition;

    /**
     * MediaPlayVFragment是否调用了onPaused暂停播放了
     */
    private boolean isPaused;

    //fragment界面
    private View mRootView;
    private JVideoView mVideoView;

    private String finalVideoUrl;

    //播放器相关监听器 VideoView暴露
//    private IMediaPlayer.OnPreparedListener mOnPreparedListener;
    //	private OnInfoListener mOnInfoListener;
    private IMediaPlayer.OnCompletionListener mOnCompletionListener;
    private IMediaPlayer.OnErrorListener mOnErrorListener;
    private IRenderView.OnSurfaceListener mOnSurfaceListener;

    //播放器相关监听器MediaController暴露
    private BaseMediaController.OnVideoPlayListener mOnVideoPlayListener;
    // controller视图
    private StarMediaController mMediaController;

    //播放器相关信息变量
    /**
     * 当前播放码率
     */
    private int currentRate;
    /**
     * 是否播放成功,加载状态条消失
     */
    private boolean isPlaySuccess;
    /**
     * 点播视频是否播放完成
     */
    private boolean isPlayComplete;
    /**
     * 是否点击了暂停按钮，视频处于暂停播放状态
     */
    private boolean isVideoPasued;
    /**
     * surfaceview是否创建成功
     */
    private boolean isSufaceCreated;

    private long startPlayTime;

    private TelephonyManager mTelephonyManager;
    private PhoneStateListener mPhoneStateListener;

    private OnPlayerListener mOnPlayerListener;

    private IRenderView.OnSlideListener mOnSlideListener;

    /**
     * 获取MediaPlayFrament实例
     *
     * @param videoUrl
     * @return
     */
    public static MediaPlayVFragment newInstance(String videoUrl) {
        MediaPlayVFragment mpFrag = new MediaPlayVFragment();
        Bundle args = new Bundle();
        args.putString("videoUrl", videoUrl);
        mpFrag.setArguments(args);
        return mpFrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		LogUtil.i(TAG,"MediaPlayVFragment onCreate");
        mTelephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//		LogUtil.i(TAG,"MediaPlayVFragment onCreateView");
        mRootView = inflater.inflate(R.layout.player_frag_star_layout, container, false);
        findViews(mRootView);
        return mRootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "MediaPlayVFragment onSaveInstanceState()");
        if (getActivity() == null) {
            Log.d(TAG, "MediaPlayVFragment onSaveInstanceState() getActivity()==null...");
            return;
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//		LogUtil.i(TAG,"MediaPlayVFragment onActivityCreated");

        if (getArguments() != null) {
            finalVideoUrl = getArguments().getString("videoUrl");
        }

        if (TextUtils.isEmpty(finalVideoUrl)) {
            Log.i(TAG, "MediaPlayVFragment onActivityCreated() getArguments().getSerializable(VideoInfo.class.getName()) is null....");
            playError(Const.ERROR_PLAY_EXCEPTION,"播放错误");
            return;
        }

        setWH();

        initListensers();

        setVideoListeners();

        initMediaController();

        listeningPhoneState();//电话监听

        normalPlay(finalVideoUrl);
    }


    private void setWH(){
        RelativeLayout.LayoutParams params= (RelativeLayout.LayoutParams) mVideoView.getLayoutParams();
//        params.width=ViewGroup.LayoutParams.MATCH_PARENT;
//        params.height=RuleUtils.getScreenWidth(getActivity())*3/4;
//        mVideoView.setLayoutParams(params);
        Log.i(TAG,"宽"+RuleUtils.getScreenWidth(getActivity())+"高"+RuleUtils.getScreenHeight(getActivity()));

//        mVideoView.setVideoLayout(IRenderView.AR_4_3_FIT_PARENT);
    }

    /**
     * 设置videoview的各种监听器
     */
    private void setVideoListeners() {
        // mVideoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);//在MediaContrller中已经监听了
//		mVideoView.setOnInfoListener(mOnInfoListener);//在MediaContrller中已经监听了
//        mVideoView.setOnPreparedListener(mOnPreparedListener);//在MediaContrller中已经监听了
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnErrorListener(mOnErrorListener);
        mVideoView.setOnSurfaceListener(mOnSurfaceListener);
        mVideoView.setOnSlideListener(mOnSlideListener);
    }

    /**
     * 初始化MediaController并提前设置,
     * 播放器未成功播放时也予显示
     */
    private void initMediaController() {
        if (mMediaController == null) {
            mMediaController = new StarMediaController(getActivity());
        }

        mMediaController.initialize();
        mMediaController.setOnVideoPlayListener(mOnVideoPlayListener);
        mVideoView.setMediaController(mMediaController);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "MediaPlayVFragment onConfigurationChanged");
    }


    private void findViews(View rootView) {
//		mRlRoot = (RelativeLayout) rootView.findViewById(R.id.rl_root_video);
        mVideoView = (JVideoView) rootView.findViewById(R.id.cmv_surfaceView);
//        pbProgressBar = (RotateLoading) rootView.findViewById(R.id.rl_loading);
//        pbProgressBar.start();
    }

    private void initListensers() {
        mOnVideoPlayListener = new BaseMediaController.OnVideoPlayListener() {

            @Override
            public void onVideoLoading() {
//                showLoading();
            }

            @Override
            public void onVideoPlay() {
                long nowTime = System.currentTimeMillis();
                Log.i(TAG, "视频地址缓冲时长：" + (nowTime - startPlayTime));

                isPlaySuccess = true;
                isPlayComplete = false;

                if (mOnPlayerListener != null) {
                    mOnPlayerListener.onPlayStart();
                }

                if (isVideoPasued || isPaused) {
                    Log.i(TAG, "mVideoView.pause()");
                    mVideoView.pause();
                }
            }

            @Override
            public void onVideoPlayComplete() {
//				LogUtil.i(TAG,"onVideoPlayComplete....onVideoPlayComplete");
                if (mOnPlayerListener != null) {
                    mOnPlayerListener.onPlayComplete();
                }
            }
        };
//        mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
//
//            @Override
//            public void onCompletion(IMediaPlayer mp) {
//                Log.i(TAG, "mOnCompletionListener....mOnCompletionListener");
//                currentPosition = 0;
//                isPlayComplete = true;
//                if (mMediaController != null) {
//                    mMediaController.stopUpdatePlayProgress();
//                }
//                if (mOnPlayerListener != null) {
//                    mOnPlayerListener.onPlayComplete();
//                }
//            }
//        };
//
//        mOnErrorListener = new IMediaPlayer.OnErrorListener() {
//            @Override
//            public boolean onError(IMediaPlayer mp, int what, int extra) {
//                Log.i(TAG, "MediaPlayVFragment onErrorListener....");
//                stopPlayer(false);
//                return true;
//            }
//        };
        mOnSurfaceListener = new IRenderView.OnSurfaceListener() {


            @Override
            public void onSurfaceCreate() {
                isSufaceCreated = true;

                Log.i("zl", "MediaPlayVFragment onSurfaceCreate finalVideoUrl = " + finalVideoUrl);
                if (!TextUtils.isEmpty(finalVideoUrl)) {
                    normalPlay(finalVideoUrl);
                }
            }

            @Override
            public void onSurfaceDestroyed() {
                isSufaceCreated = false;
                stopPlayer(false);
            }
        };
    }

    /**
     * 将fragment移除
     */
    public void removeSelf() {
        FragmentTransaction mFragTransaction = getFragTran();
        mFragTransaction.remove(this);
        mFragTransaction.commitAllowingStateLoss();
    }

    /**
     * 获取FragmentTransaction
     *
     * @return
     */
    public FragmentTransaction getFragTran() {
        FragmentTransaction mFragTransaction = null;
        if (getParentFragment() == null) {
            mFragTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        } else {
            mFragTransaction = getParentFragment().getChildFragmentManager().beginTransaction();
        }
        return mFragTransaction;
    }

    /**
     * 弹框提示播放错误
     */
    private void playError(int errNo, int errMsgId) {
        if (getActivity() == null) {
            return;
        }
        playError(errNo, getString(errMsgId));
    }

    /**
     * 弹框提示播放错误
     */
    private void playError(int errNo, String errMsg) {
        if (getActivity() == null) {
            return;
        }
        if (mOnPlayerListener != null) {
            mOnPlayerListener.onPlayError(errNo, errMsg);
        } else {
            removeSelf();
            errMsg = TextUtils.isEmpty(errMsg) ? getString(R.string.error_play_video) : errMsg;
            new AlertDialog.Builder(getActivity()).setTitle("提示").setMessage(errMsg)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            getActivity().finish();
                        }
                    }).show();
        }
    }
    /***********************start 播放器播放相关方法 start*************************/

    /**
     * 正常播放,高清或者标清
     *
     * @param path
     */
    private void normalPlay(String path) {
        if (TextUtils.isEmpty(path)) {
            Log.i(TAG, "MediaPlayVFragment normalPlay() path is null....");
            playError(Const.ERROR_PLAY_EXCEPTION, R.string.error_path_null);
            return;
        }
        finalVideoUrl = path;
        Log.i(TAG, "MediaPlayVFragment normalPlay() isSufaceCreated = " + isSufaceCreated + ",finalVideoUrl = " + finalVideoUrl);
        if (isSufaceCreated) {
            startPlayTime = System.currentTimeMillis();//用来记录播放缓冲时间
            mVideoView.setVideoPath(finalVideoUrl);
            if (currentPosition != 0) {
                mVideoView.seekTo(currentPosition);
            }
        }
    }

    /**
     * 通过videoUrl开始播放video
     *
     * @param videoUrl
     */
    public void playVideo(String videoUrl) {
//        showLoading();
        stopPlayer(true);
        if (mMediaController != null) {
            mMediaController.initialize();
        }
        finalVideoUrl = videoUrl;
//        mVideoView.reload(finalVideoUrl);
        normalPlay(finalVideoUrl);
    }

    /**
     * 释放播放器
     *
     * @param playStart 释放播放器后再次播放时是否从头播放
     */
    private void stopPlayer(boolean playStart) {
        isPlaySuccess = false;
        if (mVideoView != null) {
            currentPosition = playStart ? 0 : mVideoView.getCurrentPosition();
            mVideoView.pause();
            mVideoView.release();
        }
    }

    /**********************end 播放器播放相关方法 end******************************/
    /*******************
     * start 外部调用相关接口和相关方法 start
     ************************/
    public interface OnPlayerListener {
        /**
         * 播放缓冲完成
         */
        void onPlayStart();

        /**
         * 播放暂停改变
         *
         * @param isPaused
         */
        void onPlayPauseChanged(boolean isPaused);

        /**
         * 播放错误
         *
         * @param errorNo
         * @param errMsg
         */
        void onPlayError(int errorNo, String errMsg);

        /**
         * 播放完成
         */
        void onPlayComplete();
    }

    /**
     * 设置播放监听
     *
     * @param l
     */
    public void setOnPlayerListener(OnPlayerListener l) {
        this.mOnPlayerListener = l;
    }

    /**
     * 获取MediaController
     *
     * @return
     */
    public StarMediaController getMediaController() {
        return mMediaController;
    }

    /**
     * 设置videoview的滑动和点击事件监听
     * @param onSlideListener
     */
    public void setOnSlideListener(IRenderView.OnSlideListener onSlideListener){
        this.mOnSlideListener = onSlideListener;
        if(mVideoView != null){
            mVideoView.setOnSlideListener(mOnSlideListener);
        }
    }


    /**
     * 判断MediaPlayer是否暂停
     *
     * @return
     */
    public boolean isVideoPaused() {
        return isVideoPasued;
    }

    /*******************end 外部调用相关接口和相关方法 end**********************/

    /**
     * 监听来电
     */
    private void listeningPhoneState() {
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_IDLE) { //手机空闲
                    if (mVideoView != null && !isPaused) {
                        mVideoView.start();
                    }
                } else {//来电或者被挂起了
                    if (mVideoView != null) {
                        mVideoView.pause();
                    }
                }
            }
        };
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * 取消来电监听
     */
    private void cancelPhoneStateListen() {
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    public void onStart() {
        super.onStart();
//		LogUtil.i(TAG,"MediaPlayVFragment onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "MediaPlayVFragment onResume");
        isPaused = false;
        if (isSufaceCreated && !isVideoPasued && mVideoView != null) {
            mVideoView.start();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.i(TAG, "MediaPlayVFragment onHiddenChanged hidden = " + hidden);
        if(hidden){
            isVideoPasued = true;
            onPause();
        }else{
            isVideoPasued = false;
            onResume();
        }
    }

    public void updatePause(){
        if(!isVideoPasued){
            isVideoPasued = true;
            mVideoView.pause();
        }else{
            isVideoPasued = false;
            mVideoView.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//		LogUtil.i(TAG,"MediaPlayVFragment onPause");
        isPaused = true;
        if (mVideoView != null) {
            mVideoView.pause();
            currentPosition = mVideoView.getCurrentPosition();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
//		LogUtil.i(TAG,"MediaPlayVFragment onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//		LogUtil.i(TAG,"MediaPlayVFragment onDestroyView");

        isPlaySuccess = false;
//        hideLoading();
        if (mVideoView != null) {
            mVideoView.release();
        }
        cancelPhoneStateListen();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//		LogUtil.i(TAG,"MediaPlayVFragment onDestroy");

        stopPlayer(true);

        if(mMediaController != null){
            mMediaController.removeListeners();
        }
        mVideoView.setOnPreparedListener(null);
        mOnCompletionListener = null;
        mVideoView.setOnCompletionListener(null);
        mOnErrorListener = null;
        mVideoView.setOnErrorListener(null);
        mOnSurfaceListener = null;
        mVideoView.setOnSurfaceListener(null);

        mOnVideoPlayListener = null;
        mMediaController.setOnVideoPlayListener(null);
        mMediaController = null;
        mVideoView.setMediaController(null);

        mRootView = null;
        mVideoView = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
//		LogUtil.i(TAG,"MediaPlayVFragment onDetach");
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
