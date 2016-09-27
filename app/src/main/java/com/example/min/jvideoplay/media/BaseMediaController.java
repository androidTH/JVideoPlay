package com.example.min.jvideoplay.media;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import tv.danmaku.ijk.media.player.IMediaPlayer;


/*public void setAnchorView(ViewGroup view) ;
public void setMediaPlayer(PlayerController cn.player);
public void show();
public void setFileName(String name);
public void show(int timeout);
public boolean isShowing();
public void hide();
public void setEnabled(boolean enabled);*/

public abstract class BaseMediaController extends FrameLayout {

    private static final String TAG =BaseMediaController.class.getSimpleName();

    protected JVideoView mVideoView;
    protected PlayerController mPlayer;
    protected boolean mIsVideoPlay;
    protected boolean mIsSeekComplete = true;
    protected OnVideoPlayListener mOnVideoPlayListener;

    //播放器相关监听器 VideoView暴露
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;
    private IMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private IMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;


    public BaseMediaController(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public BaseMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public BaseMediaController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }


    protected abstract void setAnchorView(ViewGroup view);

    protected abstract boolean isShowing();

    protected abstract void show();

    protected abstract void show(int timeout);

    protected abstract void setFileName(String name);

    protected abstract void hide();

    protected abstract void toggleButtons(boolean isFullScreen);


    /**
     * 当播放器调用stopPlayback()方法时，恢复相关初始化数据
     */
    public void initialize() {
        mIsVideoPlay = false;
    }

    public void setSeekComplete(boolean isSeeckComplete) {
        mIsSeekComplete = isSeeckComplete;
    }


    public void setMediaPlayer(PlayerController player) {
        mPlayer = player;
//		updatePauseButton();
        if (mPlayer instanceof JVideoView) {
            mVideoView = (JVideoView) mPlayer;
        }
        setPlayerListeners();
    }

    protected void setPlayerListeners() {

        mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(IMediaPlayer mp) {
//				LogUtil.e(TAG, "MediaPlayVFragment:CntvPlayer videoview  onPrepared .....");
                mVideoView.start();
                if(mVideoView.getCurrentPosition() != 0){
                    mIsVideoPlay = true;
                    mOnVideoPlayListener.onVideoPlay();
                    Log.i(TAG, "BaseMediaController OnPreparedListener... mOnVideoPlayListener.onVideoPlay()");
                }

            }
        };

        mOnVideoSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {

            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width,
                                           int height, int sar_num, int sar_den) {
                // TODO Auto-generated method stub

                //本来可以直接在onInfo中处理播放逻辑，但经过测试发现，
                //首次播放时，有调用不到onInfo方法的情况，但onVideoSizeChanged方法一定能调用到
                if (mIsSeekComplete && mOnVideoPlayListener != null) {
                    mIsVideoPlay = true;
                    mPlayer.start();
                    mOnVideoPlayListener.onVideoPlay();
                    Log.i(TAG, "BaseMediaController onVideoSizeChanged... mOnVideoPlayListener.onVideoPlay()");
                }

                if (isShowing()) {//如果控制器显示了，重新显示，如果没有显示，不作操作
                    show();
                }
            }
        };

        mOnSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {

            @Override
            public void onSeekComplete(IMediaPlayer mp) {
                // TODO Auto-generated method stub
                mIsSeekComplete = true;
                Log.i(TAG, "BaseMediaController onSeekComplete + percent = " + mVideoView.getBufferPercentage());
            }
        };

        mOnInfoListener = new IMediaPlayer.OnInfoListener() {

            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                // TODO Auto-generated method stub
                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        Log.i(TAG, "MEDIA_INFO_BUFFERING_START ");
                        if (mIsVideoPlay && mOnVideoPlayListener != null) {
                            mPlayer.pause();
                            mOnVideoPlayListener.onVideoLoading();
                        }
                        break;

                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        if (mIsSeekComplete && mOnVideoPlayListener != null) {
                            mPlayer.start();
                            mOnVideoPlayListener.onVideoPlay();
                            Log.i(TAG, "BaseMediaController MEDIA_INFO_BUFFERING_END... mOnVideoPlayListener.onVideoPlay()");
                        }

                        if (isShowing()) {//如果控制器显示了，重新显示，如果没有显示，不作操作
                            show();
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        };

        mOnBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {

            @Override
            public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                // TODO Auto-generated method stub

            }
        };


        if (mVideoView != null) {
            mVideoView.setOnPreparedListener(mOnPreparedListener);
            mVideoView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
            mVideoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
            mVideoView.setOnInfoListener(mOnInfoListener);
            mVideoView.setOnSeekCompleteListener(mOnSeekCompleteListener);
        }
    }

    public void removeListeners(){
        if(mVideoView != null){
            mOnPreparedListener = null;
            mVideoView.setOnPreparedListener(null);
            mOnVideoSizeChangedListener = null;
            mVideoView.setOnVideoSizeChangedListener(null);
            mOnBufferingUpdateListener = null;
            mVideoView.setOnBufferingUpdateListener(null);
            mOnInfoListener = null;
            mVideoView.setOnInfoListener(null);
            mOnSeekCompleteListener = null;
            mVideoView.setOnSeekCompleteListener(null);

        }
    }



    public interface OnVideoPlayListener {
        void onVideoLoading();

        void onVideoPlay();

        void onVideoPlayComplete();
    }

    public void setOnVideoPlayListener(OnVideoPlayListener mVideoPlayListener) {
        this.mOnVideoPlayListener = mVideoPlayListener;
    }


}
