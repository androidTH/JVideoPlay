package com.example.min.jvideoplay.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * 尺寸大小实用工具类
 * <p/>
 * Created by Clock on 2016/1/16.
 */
public class RuleUtils {

    /**
     * 获取屏幕的宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    /**
     * 获取屏幕的高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    /**
     * 将dp转换成对应的像素值
     *
     * @param context
     * @param dp
     * @return
     */
    public static float convertDp2Px(Context context, int dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    /**
     * 将sp转换成对应的像素值
     *
     * @param context
     * @param sp
     * @return
     */
    public static float convertSp2Px(Context context, int sp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics);
    }


    /** 检查设备是否提供摄像头 */
    public static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // 摄像头存在
            return true;
        } else {
            // 摄像头不存在
            return false;
        }
    }
}
