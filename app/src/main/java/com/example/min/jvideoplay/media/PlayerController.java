package com.example.min.jvideoplay.media;

/**
 * 代替 android.widget.MediaController.MediaPlayerControl  以便修改
 * 播放控制相关方法，供界面控制器调用
 *
 * @author zengliang
 */
public interface PlayerController {
    void start();

    void pause();

    long getDuration();

    long getCurrentPosition();

    void seekTo(long newPosition);

    boolean isPlaying();

    int getBufferPercentage();

    boolean canPause();

    boolean canSeekBackward();

    boolean canSeekForward();

    void closePlayer();//关闭播放视频,使播放器处于idle状态

    /**
     * Get the audio session id for the player used by this VideoView. This can be used to
     * apply audio effects to the audio track of a video.
     *
     * @return The audio session, or 0 if there was an error.
     */
    int getAudioSessionId();

    void setFullscreen(boolean fullscreen);

    /***
     *
     * @param fullscreen
     * @param screenOrientation valid only fullscreen=true.values should be one of
     *                          ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
     *                          ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
     *                          ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT,
     *                          ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
     */
    void setFullscreen(boolean fullscreen, int screenOrientation);
}
