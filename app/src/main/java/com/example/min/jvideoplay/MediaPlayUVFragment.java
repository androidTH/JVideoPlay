package com.example.min.jvideoplay;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.example.min.jvideoplay.media.BaseMediaController;
import com.example.min.jvideoplay.media.Const;
import com.example.min.jvideoplay.media.IRenderView;
import com.example.min.jvideoplay.media.JVideoView;
import com.example.min.jvideoplay.media.StarMediaController;
import com.example.min.jvideoplay.media.UniversalMediaController;
import com.example.min.jvideoplay.media.UniversalVideoView;
import com.example.min.jvideoplay.utils.RuleUtils;

import java.lang.reflect.Field;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by min on 2016/7/22.
 */
public class MediaPlayUVFragment extends Fragment implements UniversalVideoView.VideoViewCallback{
    private static final String TAG = MediaPlayUVFragment.class.getSimpleName();


    private static final String SEEK_POSITION_KEY = "SEEK_POSITION_KEY";
    private View mRootView;
    private UniversalVideoView mVideoView;
    private UniversalMediaController mMediaController;
    private FrameLayout mVideoLayout;
    private String finalVideoUrl;

    private long mSeekPosition;
    private int cachedHeight;
    private boolean isFullscreen;

    public static MediaPlayUVFragment newInstance(String videoUrl) {
        MediaPlayUVFragment mpFrag = new MediaPlayUVFragment();
        Bundle args = new Bundle();
        args.putString("videoUrl", videoUrl);
        mpFrag.setArguments(args);
        return mpFrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_uv, container, false);
        findViews(mRootView);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState!=null){
            mSeekPosition = savedInstanceState.getInt(SEEK_POSITION_KEY);
        }

        if (getArguments() != null) {
            finalVideoUrl = getArguments().getString("videoUrl");
        }
        mVideoView.setMediaController(mMediaController);
        mVideoView.setVideoPath(finalVideoUrl);
        mVideoView.start();
        mVideoView.setVideoViewCallback(this);
        mMediaController.setTitle("Big Buck Bunny");
    }

    @Override
    public void onPause(IMediaPlayer mediaPlayer) {

    }

    @Override
    public void onStart(IMediaPlayer mediaPlayer) {

    }

    @Override
    public void onBufferingStart(IMediaPlayer mediaPlayer) {

    }

    @Override
    public void onBufferingEnd(IMediaPlayer mediaPlayer) {

    }

    @Override
    public void onScaleChange(boolean isFullscreen) {
        this.isFullscreen = isFullscreen;
        if (isFullscreen) {
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoLayout.setLayoutParams(layoutParams);

        } else {
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = this.cachedHeight;
            mVideoLayout.setLayoutParams(layoutParams);
        }

//        switchTitleBar(!isFullscreen);
    }

//    private void switchTitleBar(boolean show) {
//        android.support.v7.app.ActionBar supportActionBar =
//        if (supportActionBar != null) {
//            if (show) {
//                supportActionBar.show();
//            } else {
//                supportActionBar.hide();
//            }
//        }
//    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "MediaPlayVFragment onConfigurationChanged");
    }


    private void findViews(View rootView) {
//		mRlRoot = (RelativeLayout) rootView.findViewById(R.id.rl_root_video);
        mVideoLayout= (FrameLayout) rootView.findViewById(R.id.video_layout);
        mVideoView = (UniversalVideoView) rootView.findViewById(R.id.videoView);
        mMediaController= (UniversalMediaController) rootView.findViewById(R.id.media_controller);
//        pbProgressBar = (RotateLoading) rootView.findViewById(R.id.rl_loading);
//        pbProgressBar.start();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState Position=" + mVideoView.getCurrentPosition());
        outState.putLong(SEEK_POSITION_KEY, mSeekPosition);
    }



    //    @Override
//    public void onRestoreInstanceState(Bundle outState) {
//        super.onRestoreInstanceState(outState);
//        mSeekPosition = outState.getInt(SEEK_POSITION_KEY);
//        Log.d(TAG, "onRestoreInstanceState Position=" + mSeekPosition);
//    }


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

    /*******************end 外部调用相关接口和相关方法 end**********************/


    @Override
    public void onStart() {
        super.onStart();
//		LogUtil.i(TAG,"MediaPlayVFragment onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "MediaPlayVFragment onResume");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.i(TAG, "MediaPlayVFragment onHiddenChanged hidden = " + hidden);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause ");
        if (mVideoView != null && mVideoView.isPlaying()) {
            mSeekPosition = mVideoView.getCurrentPosition();
            Log.d(TAG, "onPause mSeekPosition=" + mSeekPosition);
            mVideoView.pause();
        }
//		LogUtil.i(TAG,"MediaPlayVFragment onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
//		LogUtil.i(TAG,"MediaPlayVFragment onStop");
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
