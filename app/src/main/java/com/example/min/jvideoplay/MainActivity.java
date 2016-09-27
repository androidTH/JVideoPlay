package com.example.min.jvideoplay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.min.jvideoplay.audio.ActivityEditVideo;
import com.example.min.jvideoplay.audio.HWRecorderActivity;
import com.example.min.jvideoplay.encoder.ActivityTestCamera;
import com.example.min.jvideoplay.utils.RuleUtils;
import com.example.min.jvideoplay.video.PlayVideoActivity;

public class MainActivity extends AppCompatActivity {

    private Button mBtnCamera;
    private Button mBtnPlay;
    private Button mBtnAudio;
    private Button mBtnEditVideo;
    private Button mBtnTestMediaCodec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    public void initView(){
        mBtnCamera= (Button) findViewById(R.id.btn_camera);
        mBtnPlay= (Button) findViewById(R.id.btn_player);
        mBtnAudio= (Button) findViewById(R.id.btn_audio);
        mBtnEditVideo= (Button) findViewById(R.id.btn_editvideo);
        mBtnTestMediaCodec= (Button) findViewById(R.id.btn_testCamera);
        mBtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(RuleUtils.checkCameraHardware(MainActivity.this)){
                    Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                    startActivity(intent);
                }
            }
        });
        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,PlayVideoActivity.class);
                startActivity(intent);
            }
        });
        mBtnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,HWRecorderActivity.class);
                startActivity(intent);
            }
        });
        mBtnEditVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ActivityEditVideo.class);
                startActivity(intent);
            }
        });
        mBtnTestMediaCodec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ActivityTestCamera.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }
}
