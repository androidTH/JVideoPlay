/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2012 YIXIA.COM

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.min.jvideoplay.media;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.example.min.jvideoplay.R;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;

import java.io.IOException;


/**
 * Displays a video file. The VideoView class can load images from various
 * sources (such as resources or content providers), takes care of computing its
 * measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting.
 * <p>
 * VideoView also provide many wrapper methods for
 * {@link #(boolean)}
 */
public class KsyVideoView extends SurfaceView implements PlayerController {
    private static final String TAG = "zl";

    private Uri mUri;
    private long mDuration;

    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private static final int STATE_SUSPEND = 6;
    private static final int STATE_RESUME = 7;
    private static final int STATE_SUSPEND_UNSUPPORTED = 8;

    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    private SurfaceHolder mSurfaceHolder = null;
    private KSYMediaPlayer ksyMediaPlayer = null;
    protected int mVideoWidth;
    protected int mVideoHeight;
    private int mVideoSarNum;
    private int mVideoSarDen;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    //    private CntvController mMediaController;
    protected BaseMediaController mMediaController;
    private View mMediaBufferingIndicator;
    private IMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private IMediaPlayer.OnCompletionListener mOnCompletionListener;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;
    private IMediaPlayer.OnErrorListener mOnErrorListener;
    private IMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;

    private IRenderView.OnSurfaceListener mOnSurfaceListener;
    private IRenderView.OnSlideListener mOnSlideListener;

    private int mCurrentBufferPercentage;
    private long mSeekWhenPrepared;
    private boolean mCanPause = true;
    private boolean mCanSeekBack = true;
    private boolean mCanSeekForward = true;
    protected Context mContext;

    private boolean canSlide = true;
    protected int mVideoLayoutType = IRenderView.AR_4_3_FIT_PARENT;
    private boolean isLooping;

    private int startX;
    private int startY;
    private int moveX;
    private int moveY;
    /**
     * 是否滑动开始
     */
    private boolean isSlideStart;
    /**
     * 左右滑动时的阻力系数(较小)
     */
    private static final float RADIO_H = 2.3f;
    /**
     * 上下滑动时的阻力系数(较大)
     */
    private static final float RADIO_V = 23f;
    /**
     * 当滑动大于mDistanceToSlide的值时，代表滑动开始，否则视为点击
     */
    private int mDistanceToSlide = 12;
    /**
     * 滑动方向，左、右、上、下
     */
    private int slideState = IRenderView.SLIDE_STATE_NONE;
    /**
     * 滑动方向，横向、竖向
     */
    private int currentSlideState = IRenderView.SLIDE_STATE_NONE;
    /**
     * 横向移动距离
     */
    private int distanceH = 0;
    /**
     * 竖向移动距离
     */
    private int distanceV = 0;


    private MeasureHelper mMeasureHelper;
    private int mAudioSession;

    public KsyVideoView(Context context) {
        super(context);
        initVideoView(context);
    }

    public KsyVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initVideoView(context);
    }

    public KsyVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initVideoView(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mMeasureHelper.getMeasuredWidth(), mMeasureHelper.getMeasuredHeight());
    }


    public void setVideoSize(int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
            requestLayout();
        }
    }

    public void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen) {
        if (videoSarNum > 0 && videoSarDen > 0) {
            mMeasureHelper.setVideoSampleAspectRatio(videoSarNum, videoSarDen);
            requestLayout();
        }
    }

    /**
     * Set the display options
     *
     * @param layoutType
     */
    public void setVideoLayout(int layoutType) {
        mVideoLayoutType = layoutType;
        mMeasureHelper.setAspectRatio(layoutType);
        requestLayout();
    }

    private void initVideoView(Context ctx) {
        mContext = ctx;
        mVideoWidth = 0;
        mVideoHeight = 0;
        mVideoSarNum = 0;
        mVideoSarDen = 0;
        getHolder().addCallback(mSHCallback);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
        mMeasureHelper = new MeasureHelper(this);
        mDistanceToSlide = getResources().getDimensionPixelOffset(R.dimen.distance_call_to_slide);
        Log.i(TAG,"initVideoView mDistanceToSlide: " + mDistanceToSlide);
        if (ctx instanceof Activity) {
            ((Activity) ctx).setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }
    }

    public boolean isValid() {
        return (mSurfaceHolder != null && mSurfaceHolder.getSurface().isValid());
    }

    public void reload(String path) {
        if (ksyMediaPlayer != null) {
            ksyMediaPlayer.reload(path);
        }
    }

    public void setLooping(boolean looping) {
        isLooping = looping;
    }

    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri) {
        mUri = uri;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void release() {
        if (ksyMediaPlayer != null) {
            ksyMediaPlayer.stop();
            ksyMediaPlayer.release();
            ksyMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            mVideoWidth = 0;
            mVideoHeight = 0;
            mVideoSarNum = 0;
            mVideoSarDen = 0;
        }
        if (mMediaController != null) {
            mMediaController.initialize();
            mMediaController.setMediaPlayer(null);
        }
    }

    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null)
            return;

        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);

        release(false);
        try {
            mDuration = -1;
            mCurrentBufferPercentage = 0;

            ksyMediaPlayer = new KSYMediaPlayer.Builder(getContext()).build();
            ksyMediaPlayer.setLooping(isLooping);
//            ksyMediaPlayer.setOption(KSYMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
//            ksyMediaPlayer.setOption(KSYMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);

            ksyMediaPlayer.setOnPreparedListener(mPreparedListener);
            ksyMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            ksyMediaPlayer.setOnCompletionListener(mCompletionListener);
            ksyMediaPlayer.setOnErrorListener(mErrorListener);
            ksyMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            ksyMediaPlayer.setOnInfoListener(mInfoListener);
            ksyMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);

            if (mUri != null)
                ksyMediaPlayer.setDataSource(mUri.toString());
            ksyMediaPlayer.setDisplay(mSurfaceHolder);
            ksyMediaPlayer.setScreenOnWhilePlaying(true);
            ksyMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            attachMediaController();
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e(TAG, "Unable to open content: " + mUri);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(ksyMediaPlayer, IMediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            Log.e(TAG, "Unable to open content: " + mUri);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(ksyMediaPlayer,
                    IMediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
    }

    @Override
    public void setSystemUiVisibility(int visibility) {
        super.setSystemUiVisibility(visibility);
    }

    public void setMediaController(BaseMediaController controller) {
        if (mMediaController != null)
            mMediaController.hide();
        mMediaController = controller;
        attachMediaController();
    }

    public void setMediaBufferingIndicator(View mediaBufferingIndicator) {
        if (mMediaBufferingIndicator != null)
            mMediaBufferingIndicator.setVisibility(View.GONE);
        mMediaBufferingIndicator = mediaBufferingIndicator;
    }

    private void attachMediaController() {
        if (ksyMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            ViewGroup anchorView = null;
            if (this.getParent() instanceof ViewGroup) {
                anchorView = (ViewGroup) (this.getParent());
                anchorView.setOnTouchListener(mOnTouchListener);
            } else {
                //可以抛出异常
            }
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlaybackState());
        }
    }

    IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {

            int videoWidth = mp.getVideoWidth();
            int videoHeight = mp.getVideoHeight();
            mVideoSarNum = sarNum;
            mVideoSarDen = sarDen;
            boolean isVideoSizeChanged = (videoWidth != mVideoWidth || videoHeight != mVideoHeight);
            if (videoWidth != 0 && videoHeight != 0 && isVideoSizeChanged) {
                Log.e(TAG, "KsyVideoView onVideoSizeChanged: videoWidth = " + videoWidth + ",videoHeight = " + videoHeight);
                setVideoSize(videoWidth, videoHeight);
                setVideoSampleAspectRatio(sarNum, sarDen);
                if (mOnVideoSizeChangedListener != null) {
                    mOnVideoSizeChangedListener.onVideoSizeChanged(mp, width, height, sarNum, sarDen);
                }
                mVideoWidth = videoWidth;
                mVideoHeight = videoHeight;
                setVideoLayout(mVideoLayoutType);
            }
        }
    };

    IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        public void onPrepared(IMediaPlayer mp) {
            mCurrentState = STATE_PREPARED;
            mTargetState = STATE_PLAYING;

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(ksyMediaPlayer);
            }
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }

            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            Log.e(TAG, "KsyVideoView onPrepared: mVideoWidth = " + mVideoWidth + ",mVideoHeight = " + mVideoHeight);

            long seekToPosition = mSeekWhenPrepared;
            if (seekToPosition != 0)
                seekTo(seekToPosition);
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                setVideoSize(mVideoWidth, mVideoHeight);
                setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                if (mSurfaceWidth == mVideoWidth
                        && mSurfaceHeight == mVideoHeight) {
                    if (mTargetState == STATE_PLAYING) {
                        start();
                        if (mMediaController != null)
                            mMediaController.show();
                    } else if (!isPlaying()
                            && (seekToPosition != 0 || getCurrentPosition() > 0)) {
                        if (mMediaController != null)
                            mMediaController.show(0);
                    }
                }
            } else if (mTargetState == STATE_PLAYING) {
                start();
            }
        }
    };

    private IMediaPlayer.OnCompletionListener mCompletionListener = new IMediaPlayer.OnCompletionListener() {
        public void onCompletion(IMediaPlayer mp) {
            Log.d(TAG, "onCompletion");
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
            if (mMediaController != null)
                mMediaController.hide();
            if (mOnCompletionListener != null)
                mOnCompletionListener.onCompletion(ksyMediaPlayer);
        }
    };

    private IMediaPlayer.OnErrorListener mErrorListener = new IMediaPlayer.OnErrorListener() {
        public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (mMediaController != null)
                mMediaController.hide();

            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(ksyMediaPlayer, framework_err, impl_err))
                    return true;
            }
            return true;
        }
    };

    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            mCurrentBufferPercentage = percent;
            if (mOnBufferingUpdateListener != null)
                mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
        }
    };

    private IMediaPlayer.OnInfoListener mInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mp, what, extra);
            } else if (ksyMediaPlayer != null) {
                if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    Log.i(TAG, "onInfo: (MEDIA_INFO_BUFFERING_START)");
                    if (mMediaBufferingIndicator != null)
                        mMediaBufferingIndicator.setVisibility(View.VISIBLE);
                } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    Log.i(TAG, "onInfo: (MEDIA_INFO_BUFFERING_END)");
                    if (mMediaBufferingIndicator != null)
                        mMediaBufferingIndicator.setVisibility(View.GONE);
                }
            }

            return true;
        }
    };

    private IMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            Log.d(TAG, "onSeekComplete");
            if (mOnSeekCompleteListener != null)
                mOnSeekCompleteListener.onSeekComplete(mp);
        }
    };

    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    public void setOnErrorListener(IMediaPlayer.OnErrorListener l) {
        mOnErrorListener = l;
    }

    public void setOnBufferingUpdateListener(IMediaPlayer.OnBufferingUpdateListener l) {
        mOnBufferingUpdateListener = l;
    }

    public void setOnSeekCompleteListener(IMediaPlayer.OnSeekCompleteListener l) {
        mOnSeekCompleteListener = l;
    }

    public void setOnInfoListener(IMediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }

    public void setOnVideoSizeChangedListener(IMediaPlayer.OnVideoSizeChangedListener l) {
        mOnVideoSizeChangedListener = l;
    }

    public void setOnSurfaceListener(IRenderView.OnSurfaceListener l) {
        this.mOnSurfaceListener = l;
    }

    public void setOnSlideListener(IRenderView.OnSlideListener l) {
        this.mOnSlideListener = l;
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
//        	Log.i("zl","SurfaceHolder.Callback surfaceChanged");
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            Log.e("zl", "surfaceChanged() mSurfaceWidth = " + mSurfaceWidth + ",mSurfaceHeight = " + mSurfaceHeight);
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
            if (ksyMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
//			Log.i("zl","SurfaceHolder.Callback surfaceCreated");
            mSurfaceHolder = holder;
            if (mOnSurfaceListener != null) {
                mOnSurfaceListener.onSurfaceCreate();
            } else if (ksyMediaPlayer != null && mCurrentState == STATE_SUSPEND
                    && mTargetState == STATE_RESUME) {
                ksyMediaPlayer.setDisplay(mSurfaceHolder);
                resume();
            } else {
                openVideo();
            }

        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // after we return from this we can't use the surface any more
//			Log.i("zl","SurfaceHolder.Callback surfaceDestroyed");
            // if (mMediaController != null) mMediaController.hide();
            mSurfaceHolder = null;
            if (mOnSurfaceListener != null) {
                mOnSurfaceListener.onSurfaceDestroyed();
            } else {
                release(true);
            }
        }
    };

    private void release(boolean cleartargetstate) {
        if (ksyMediaPlayer != null) {
            ksyMediaPlayer.reset();
            ksyMediaPlayer.release();
            ksyMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate)
                mTargetState = STATE_IDLE;
        }
    }

    OnTouchListener mOnTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent ev) {
            // TODO Auto-generated method stub
            if (ksyMediaPlayer == null) {
                return false;
            }
            if (mOnSlideListener == null || !canSlide) {
                toggleMediaControlsVisiblity();
                return false;
            } else {
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//	    			Log.i("zl", "MotionEvent.ACTION_DOWN");
                        startX = (int) ev.getX();
                        startY = (int) ev.getY();

                        break;
                    case MotionEvent.ACTION_MOVE:
//	    			Log.i("zl", "MotionEvent.ACTION_MOVE");
                        moveX = (int) ev.getX();
                        moveY = (int) ev.getY();

                        // 当前触摸的点到上一个触摸点的位移
                        int diffX = moveX - startX;
                        int diffY = moveY - startY;

                        if (Math.abs(diffY) < Math.abs(diffX) && Math.abs(diffX) >= mDistanceToSlide) {//代表左右滑动

                            if (currentSlideState != IRenderView.SLIDE_STATE_VERTICAL) {//防止横向滑动的时候未松手又纵向滑动
                                currentSlideState = IRenderView.SLIDE_STATE_HORIZONRAL;
                                slideState = diffX > 0 ? IRenderView.SLIDE_STATE_RIGHT : IRenderView.SLIDE_STATE_LEFT;
                                diffX /= RADIO_H;
                                if (!isSlideStart) {
                                    isSlideStart = true;
                                    mOnSlideListener.onSlideStart(slideState);
                                }
                                if (diffX != distanceH) {
                                    distanceH = diffX;
                                    mOnSlideListener.onSliding(slideState, distanceH);
                                }
                            }

                        } else if (Math.abs(diffY) > Math.abs(diffX) && Math.abs(diffY) >= mDistanceToSlide) {//代表上下滑动
                            if (currentSlideState != IRenderView.SLIDE_STATE_HORIZONRAL) {//防止纵向滑动的时候未松手又横向滑动
                                currentSlideState = IRenderView.SLIDE_STATE_VERTICAL;
                                slideState = diffY > 0 ? IRenderView.SLIDE_STATE_DOWN : IRenderView.SLIDE_STATE_UP;
                                diffY /= RADIO_V;

                                if (!isSlideStart) {
                                    isSlideStart = true;
                                    mOnSlideListener.onSlideStart(slideState);
                                }
                                if (diffY != distanceV) {
                                    distanceV = diffY;
                                    mOnSlideListener.onSliding(slideState, distanceV);
                                }
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
//	    			Log.i("zl", "MotionEvent.ACTION_UP");
                        moveX = (int) ev.getX();
                        moveY = (int) ev.getY();

                        isSlideStart = false;
                        if (currentSlideState == IRenderView.SLIDE_STATE_HORIZONRAL) {
                            mOnSlideListener.onSlideEnd(slideState, distanceH);
                            distanceH = 0;
                            slideState = IRenderView.SLIDE_STATE_NONE;
                            currentSlideState = IRenderView.SLIDE_STATE_NONE;
                        } else if (currentSlideState == IRenderView.SLIDE_STATE_VERTICAL) {
                            mOnSlideListener.onSlideEnd(slideState, distanceV);
                            distanceV = 0;
                            slideState = IRenderView.SLIDE_STATE_NONE;
                            currentSlideState = IRenderView.SLIDE_STATE_NONE;
                        } else if (mMediaController != null) {
//                            toggleMediaControlsVisiblity();
                            mOnSlideListener.onClick();
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        Log.i("zl", "MotionEvent.ACTION_CANCEL");
                        slideState = IRenderView.SLIDE_STATE_NONE;
                        currentSlideState = IRenderView.SLIDE_STATE_NONE;
                        mOnSlideListener.onSlideEnd(slideState, 0);
                        distanceV = 0;
                        distanceH = 0;

                        break;
                    default:
                        break;
                }
                return true;
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null)
            toggleMediaControlsVisiblity();
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return onKey(keyCode, event);
        //return super.onKeyDown(keyCode, event);
    }

    protected boolean onKey(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK
                && keyCode != KeyEvent.KEYCODE_VOLUME_UP
                && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
                && keyCode != KeyEvent.KEYCODE_MENU
                && keyCode != KeyEvent.KEYCODE_CALL
                && keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported
                && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                    || keyCode == KeyEvent.KEYCODE_SPACE) {
                if (ksyMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    && ksyMediaPlayer.isPlaying()) {
                pause();
                mMediaController.show();
            } else {
                toggleMediaControlsVisiblity();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController == null) {
            return;
        }
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();

        }
    }

    @Override
    public void start() {
        if (isInPlaybackState()) {
            ksyMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (ksyMediaPlayer.isPlaying()) {
                ksyMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void resume() {
        if (mSurfaceHolder == null && mCurrentState == STATE_SUSPEND) {
            mTargetState = STATE_RESUME;
        } else if (mCurrentState == STATE_SUSPEND_UNSUPPORTED) {
            openVideo();
        }
    }

    @Override
    public long getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0)
                return (int) mDuration;
            mDuration = ksyMediaPlayer.getDuration();
            return (int) mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    @Override
    public long getCurrentPosition() {
        if (isInPlaybackState()) {
            long position = ksyMediaPlayer.getCurrentPosition();
            Log.i("kooMEDIA", "KsyVideoView position = " + position);
            return position;
        }
        return 0;
    }

    @Override
    public void seekTo(long msec) {
        if (isInPlaybackState()) {
            if (mMediaController != null) {
                mMediaController.setSeekComplete(false);
            }
            ksyMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && ksyMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (ksyMediaPlayer != null)
            return mCurrentBufferPercentage;
        return 0;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    protected boolean isInPlaybackState() {
        return (ksyMediaPlayer != null && mCurrentState != STATE_ERROR
                && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    public boolean canPause() {
        return mCanPause;
    }

    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public int getAudioSessionId() {
        return mAudioSession;
    }

    public void canSlide(boolean canSlide) {
        this.canSlide = canSlide;
    }

    @Override
    public void closePlayer() {

    }

    @Override
    public void setFullscreen(boolean fullscreen) {

    }

    @Override
    public void setFullscreen(boolean fullscreen, int screenOrientation) {

    }
}
