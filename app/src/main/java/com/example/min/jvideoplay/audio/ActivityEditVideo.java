package com.example.min.jvideoplay.audio;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.min.jvideoplay.R;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class ActivityEditVideo extends AppCompatActivity {

    private static String TAG=ActivityEditVideo.class.getSimpleName();

    private String path= Environment.getExternalStorageDirectory().getPath();

    private MediaExtractor mMediaExtractor;
    private MediaMuxer mMediaMuxer;
    private int framerate;
    private MediaFormat format;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_edit_video);
        new Thread(new Runnable() {
            @Override
            public void run() {
                getEditVideo();
            }
        }).start();
    }


    private void getEditVideo(){
        Log.i(TAG,"路径="+path);
        int mVideoTrackIndex = -1;
        mMediaExtractor=new MediaExtractor();
        try {
            mMediaExtractor.setDataSource(path+"/input.mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }
        int videoTrack=getVideoTrack(mMediaExtractor);
        try {
            mMediaMuxer=new MediaMuxer(path+"/output.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mVideoTrackIndex=mMediaMuxer.addTrack(format);
            mMediaMuxer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaCodec.BufferInfo bufferInfo=new MediaCodec.BufferInfo();
        bufferInfo.presentationTimeUs=0;
        ByteBuffer buffer=ByteBuffer.allocate(1024*256);
        while(true){
            int len=mMediaExtractor.readSampleData(buffer,0);
            if(len<0){
                break;
            }
            mMediaExtractor.advance();
            bufferInfo.flags=MediaCodec.BUFFER_FLAG_SYNC_FRAME;
            bufferInfo.size=len;
            bufferInfo.presentationTimeUs+=1000*1000/framerate;
            mMediaMuxer.writeSampleData(mVideoTrackIndex,buffer,bufferInfo);
        }
        mMediaExtractor.release();
        if(mMediaMuxer!=null){
            mMediaMuxer.stop();
            mMediaMuxer.release();
        }
    }

    private int getVideoTrack(MediaExtractor extractor){
        for(int i=0;i<extractor.getTrackCount();i++){
            format=extractor.getTrackFormat(i);
            String type=format.getString(MediaFormat.KEY_MIME);
            if(type.startsWith("video/")){
                extractor.selectTrack(i);
                framerate=format.getInteger(MediaFormat.KEY_FRAME_RATE);//帧率
                return i;
            }
        }
        return -1;
    };
}
