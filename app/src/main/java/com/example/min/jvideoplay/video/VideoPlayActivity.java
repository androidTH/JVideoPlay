package com.example.min.jvideoplay.video;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.example.min.jvideoplay.R;
import com.example.min.jvideoplay.adapter.VideoFilterAdapter;
import com.example.min.jvideoplay.bean.VideoFilterBean;
import com.example.min.jvideoplay.media.IRenderView;
import com.example.min.jvideoplay.media.JVideoView;
import com.example.min.jvideoplay.media.KsyVideoView;
import com.example.min.jvideoplay.media.VideoPlayerGLSurfaceView;

import java.util.List;

public class VideoPlayActivity extends AppCompatActivity {

    private static String TAG=VideoPlayActivity.class.getSimpleName();

    public static String VIDEOPATH = "videoPath";
    private static final int STATE_NOMAL = 0;
    private static final int STATE_PROCESS_VIDEO = 1;
    private static final int STATE_SHARE_PREPARING = 3;
    private static final int STATE_UPLOADING_PREPARING = 4;
    private static final int STATE_UPLOADING = 5;
    private static final int STATE_UPLOADED = 6;

    private VideoPlayerGLSurfaceView mVideoPlayerGlSurfaceView;
//    private JVideoView mVideoPlayerGlSurfaceView;
//    private KsyVideoView mVideoPlayerGlSurfaceView;
    private RecyclerView mRecyclerView;
    private VideoFilterAdapter mVideoFilterAdapter;

    private List<VideoFilterBean> mVideoFilters;
    private int mCurrentFilterPosition;
    private int mCurrentEditState = STATE_NOMAL;
    private String mVideoInputPath,mVideoOutputPath, mStickerPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.content_video_play);
        initView();
    }

    private void initView(){
        mVideoInputPath = getIntent().getStringExtra(VIDEOPATH);
        Log.i(TAG,"路径"+mVideoInputPath);
        mVideoPlayerGlSurfaceView= (VideoPlayerGLSurfaceView) findViewById(R.id.videoplayer_layout);
//        mVideoPlayerGlSurfaceView= (JVideoView) findViewById(R.id.videoplayer_layout);
//        mVideoPlayerGlSurfaceView= (KsyVideoView) findViewById(R.id.videoplayer_layout);
        mRecyclerView= (RecyclerView) findViewById(R.id.rcv_filter_list);
        LinearLayoutManager mLinearLayoutManager=new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mVideoFilters = VideoFilterBean.initData();
        if (mVideoFilterAdapter == null) {
            mVideoFilterAdapter = new VideoFilterAdapter(this, mVideoFilters);
            mRecyclerView.setAdapter(mVideoFilterAdapter);
            mVideoFilterAdapter.setOnFilterConfigClickListener(new VideoFilterAdapter.OnFilterConfigClickListener() {
                @Override
                public void onFilterConfigClick(int position) {
                    if (mCurrentEditState == STATE_PROCESS_VIDEO) {
                        return;
                    }
                    mCurrentFilterPosition = position;
                    mVideoPlayerGlSurfaceView.setEffect(mVideoFilters.get(position).getShaderInterface());
                }
            });
        } else {
            mVideoFilterAdapter.notifyDataSetChanged();
        }
        startPlay();
    }

    public void startPlay(){
        mVideoPlayerGlSurfaceView.setZOrderOnTop(false);
        mVideoPlayerGlSurfaceView.setZOrderMediaOverlay(true);
        mVideoPlayerGlSurfaceView.setLooping(true);
//        mVideoPlayerGlSurfaceView.setVideoLayout(IRenderView.AR_16_9_FIT_PARENT);
        mVideoPlayerGlSurfaceView.setVideoPath(mVideoInputPath);
//        mVideoPlayerGlSurfaceView.setVideoPath("http://starchat.ks3-cn-beijing.ksyun.com/record/live/afd4836712c5e77550897e25711e1d96/hls/afd4836712c5e77550897e25711e1d96-699.m3u8");
//        mVideoPlayerGlSurfaceView.setVideoPath("http://videos.xingliaoapp.com/videos/14690927432320.m3u8");
    }
}
