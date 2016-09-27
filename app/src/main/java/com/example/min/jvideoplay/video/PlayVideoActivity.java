package com.example.min.jvideoplay.video;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.min.jvideoplay.MediaPlayUVFragment;
import com.example.min.jvideoplay.MediaPlayVFragment;
import com.example.min.jvideoplay.R;
import com.example.min.jvideoplay.media.IRenderView;

public class PlayVideoActivity extends AppCompatActivity {


    private MediaPlayVFragment mPlayVFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        initFrameLayout();
    }

    public void initFrameLayout(){
//        mPlayVFragment=MediaPlayVFragment.newInstance("http://videos.xingliaoapp.com/videos/14690927432320.m3u8");
        mPlayVFragment= MediaPlayVFragment.newInstance("http://videos.xingliaoapp.com/videos/14690927432320.m3u8");
        FragmentTransaction manager=getSupportFragmentManager().beginTransaction();
        manager.add(R.id.fl_player,mPlayVFragment);
        manager.commit();
        mPlayVFragment.setOnPlayerListener(new MediaPlayVFragment.OnPlayerListener() {
            @Override
            public void onPlayStart() {

            }

            @Override
            public void onPlayPauseChanged(boolean isPaused) {

            }

            @Override
            public void onPlayError(int errorNo, String errMsg) {

            }

            @Override
            public void onPlayComplete() {

            }
        });
        mPlayVFragment.setOnSlideListener(new IRenderView.OnSlideListener() {
            @Override
            public void onClick() {

            }

            @Override
            public void onSlideStart(int direction) {

            }

            @Override
            public void onSliding(int direction, int distance) {

            }

            @Override
            public void onSlideEnd(int direction, int distance) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
