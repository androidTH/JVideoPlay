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
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.View;


import com.sherazkhilji.videffect.NoEffect;
import com.sherazkhilji.videffect.interfaces.ShaderInterface;

import java.io.IOException;

/**
 * Displays a video file. The VideoView class can load images from various
 * sources (such as resources or content providers), takes care of computing its
 * measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting.
 * <p/>
 * VideoView also provide many wrapper methods for
 * {@link #(boolean)}
 */
public class VideoPlayerGLSurfaceView extends GLSurfaceView implements VideoRender.OnSurfaceListener {
    private static final String TAG = VideoPlayerGLSurfaceView.class.getSimpleName();

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

    private Surface mSurface = null;
    private MediaPlayer mMediaPlayer = null;
    private boolean isLooping;
    protected int mVideoWidth;
    protected int mVideoHeight;
    private View mMediaBufferingIndicator;
    private MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private MediaPlayer.OnErrorListener mOnErrorListener;
    private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    private MediaPlayer.OnInfoListener mOnInfoListener;
    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;

    private int mCurrentBufferPercentage;
    private int mSeekWhenPrepared;
    protected Context mContext;

    private VideoRender mRenderer;
    private ShaderInterface mEffect;

    private MeasureHelper mMeasureHelper;


    public VideoPlayerGLSurfaceView(Context context) {
        super(context);
        initSurfaceView(context);
    }

    public VideoPlayerGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSurfaceView(context);
    }

    private void initSurfaceView(Context ctx) {
        mContext = ctx;
        mVideoWidth = 0;
        mVideoHeight = 0;
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
        if (ctx instanceof Activity) {
            ((Activity) ctx).setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }

        mMeasureHelper = new MeasureHelper(this);

        setEGLContextClientVersion(2);
        mRenderer = new VideoRender(mContext);
        setEffect(null);
        mRenderer.setOnSurfaceListener(this);
        setRenderer(mRenderer);
    }

    /**
     * 设置滤镜
     * @param shaderEffect
     */
    public void setEffect(ShaderInterface shaderEffect) {
        if (shaderEffect == null) {
            mEffect = new NoEffect();
        } else {
            mEffect = shaderEffect;
        }
        mRenderer.setShaderSource(mEffect.getShader(this));
    }

    @Override
    public void onSurfaceCreate(Surface surface) {
        Log.i(TAG, "VideoPlayGLSurfaceView onSurfaceCreate()");
        mSurface = surface;
        openVideo();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mMeasureHelper.getMeasuredWidth(), mMeasureHelper.getMeasuredHeight());
    }

    public void setVideoSize(int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
//            getHolder().setFixedSize(videoWidth, videoHeight);
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
        mMeasureHelper.setAspectRatio(layoutType);
        requestLayout();
    }


    public boolean isValid() {
        return (mSurface != null && mSurface.isValid());
    }

    public void setLooping(boolean looping){
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
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            mVideoWidth = 0;
            mVideoHeight = 0;
        }
    }

    private void openVideo() {
        if (mUri == null || mSurface == null)
            return;

        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);

        release(false);
        try {
            mDuration = -1;
            mCurrentBufferPercentage = 0;
            // mMediaPlayer = new AndroidMediaPlayer();
            mMediaPlayer = null;
            if (mUri != null) {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setLooping(isLooping);
            }
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
            if (mUri != null)
                mMediaPlayer.setDataSource(mUri.toString());
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            Log.i(TAG, "prepareAsync complete");
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e(TAG, "Unable to open content: " + mUri);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer,
                    MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            Log.e(TAG, "Unable to open content: " + mUri);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer,
                    MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
    }

    public void setMediaBufferingIndicator(View mediaBufferingIndicator) {
        if (mMediaBufferingIndicator != null)
            mMediaBufferingIndicator.setVisibility(View.GONE);
        mMediaBufferingIndicator = mediaBufferingIndicator;
    }

    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            int videoWidth = mp.getVideoWidth();
            int videoHeight = mp.getVideoHeight();
            boolean isVideoSizeChanged = (videoWidth != mVideoWidth || videoHeight != mVideoHeight);
            if (videoWidth != 0 && videoHeight != 0 && isVideoSizeChanged) {
                Log.e(TAG, "VideoPlayGLSurfaceView onVideoSizeChanged: videoWidth = " + videoWidth + ",videoHeight = " + videoHeight);
                setVideoSize(videoWidth, videoHeight);
                if (mOnVideoSizeChangedListener != null) {
                    mOnVideoSizeChangedListener.onVideoSizeChanged(mp, width, height);
                }
            }
        }
    };

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            Log.d(TAG, "onPrepared");
            mCurrentState = STATE_PREPARED;
            mTargetState = STATE_PLAYING;

            if (mOnPreparedListener != null)
                mOnPreparedListener.onPrepared(mMediaPlayer);
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            int seekToPosition = mSeekWhenPrepared;
            if (seekToPosition != 0)
                seekTo(seekToPosition);
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                setVideoSize(mVideoWidth, mVideoHeight);
                if (mTargetState == STATE_PLAYING) {
                    start();
                }
            } else if (mTargetState == STATE_PLAYING) {
                start();
            }
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            Log.d(TAG, "onCompletion");
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
            if (mOnCompletionListener != null)
                mOnCompletionListener.onCompletion(mMediaPlayer);
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            Log.e(TAG, "onError");
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err))
                    return true;
            }
            return true;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mCurrentBufferPercentage = percent;
            if (mOnBufferingUpdateListener != null)
                mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
        }
    };

    private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mp, what, extra);
            } else if (mMediaPlayer != null) {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    Log.i(TAG, "onInfo: (MEDIA_INFO_BUFFERING_START)");
                    if (mMediaBufferingIndicator != null)
                        mMediaBufferingIndicator.setVisibility(View.VISIBLE);
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    Log.i(TAG, "onInfo: (MEDIA_INFO_BUFFERING_END)");
                    if (mMediaBufferingIndicator != null)
                        mMediaBufferingIndicator.setVisibility(View.GONE);
                }
            }

            return true;
        }
    };

    private MediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            Log.d(TAG, "onSeekComplete");
            if (mOnSeekCompleteListener != null)
                mOnSeekCompleteListener.onSeekComplete(mp);
        }
    };

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener l) {
        mOnErrorListener = l;
    }

    public void setOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener l) {
        mOnBufferingUpdateListener = l;
    }

    public void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener l) {
        mOnSeekCompleteListener = l;
    }

    public void setOnInfoListener(MediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }

    public void setOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener l) {
        mOnVideoSizeChangedListener = l;
    }

    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate)
                mTargetState = STATE_IDLE;
        }
    }

    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void resume() {
        if (mSurface == null && mCurrentState == STATE_SUSPEND) {
            mTargetState = STATE_RESUME;
        } else if (mCurrentState == STATE_SUSPEND_UNSUPPORTED) {
            openVideo();
        }
    }

    public long getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0)
                return (int) mDuration;
            mDuration = mMediaPlayer.getDuration();
            return (int) mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    public long getCurrentPosition() {
        if (isInPlaybackState()) {
            long position = mMediaPlayer.getCurrentPosition();
            Log.i(TAG, "VideoPlayGLSurfaceView position = " + position);
            return position;
        }
        return 0;
    }

    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    public int getBufferPercentage() {
        if (mMediaPlayer != null)
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
        return (mMediaPlayer != null && mCurrentState != STATE_ERROR
                && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }
}
