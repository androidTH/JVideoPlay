package com.example.min.jvideoplay.media;

/**
 * Created by min on 2016/7/26.
 */
public interface MediaPlayerControl {
    void start();

    void pause();

    long getDuration();

    long getCurrentPosition();

    void seekTo(long pos);

    boolean isPlaying();

    int getBufferPercentage();

    boolean canPause();

    boolean canSeekBackward();

    boolean canSeekForward();

    void closePlayer();//关闭播放视频,使播放器处于idle状态

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
