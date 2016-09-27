package com.example.min.jvideoplay.media;

/**
 * 常量类
 *
 * @author zengliang
 */
public class Const {

    public static final String URLPRE = "mediaplayer";

    /**
     * 默认半屏时，播放器视图宽高比4:3,
     * 相对ASPECT_RATIO_LARGE，高度更大
     */
    public static final float ASPECT_RATIO_SMALL = 4 / 3f;
    /**
     * 播放器视图宽高比16:9
     * 相对ASPECT_RATIO_SMALL，高度更小
     */
    public static final float ASPECT_RATIO_LARGE = 16 / 9f;

    /**
     * 开关控制，代表关闭
     */
    public static final int CLOSE = 0;
    /**
     * 开关控制 ，代表开启
     */
    public static final int OPEN = 1;


    // 码率,值对应码率地址中的 {xxx}.m3u8
    /**
     * 极速模式,200.m3u8
     */
    public static final int PLAYER_MODE_TS = 200;
    /**
     * 标清模式,450.m3u8
     */
    public static final int PLAYER_MODE_SD = 450;
    /**
     * 高清模式,850.m3u8
     */
    public static final int PLAYER_MODE_HD = 850;
    /**
     * 超清模式,1200.m3u8
     */
    public static final int PLAYER_MODE_PD = 1200;
    /**
     * 超高清模式,2000.m3u8
     */
    public static final int PLAYER_MODE_UHD = 2000;
    /**
     * 自适应模式,用于直播
     */
    public static final int PLAYER_MODE_AUTO = 205;

    //播放错误类型
    /**
     * 网络异常
     */
    public static final int ERROR_NET_EXCEPTION = 501;
    /**
     * 访问超时异常
     */
    public static final int ERROR_TIMEOUT_EXCEPTION = 502;

    /**
     * 视频播放异常
     */
    public static final int ERROR_PLAY_EXCEPTION = 503;


}
