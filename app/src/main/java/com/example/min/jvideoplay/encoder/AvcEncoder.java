package com.example.min.jvideoplay.encoder;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class AvcEncoder {
    private final static String TAG = "MeidaCodec";

    private int TIMEOUT_USEC = 12000;

    private MediaCodec mediaCodec;
    private Surface mSurface;
    int m_width;
    int m_height;
    int m_framerate;
    byte[] m_info = null;

    public byte[] configbyte;


    @SuppressLint("NewApi")
    public AvcEncoder(int width, int height, int framerate, int bitrate) {

        m_width = width;
        m_height = height;
        m_framerate = framerate;

        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 5);//设置视频的bit率
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mSurface=mediaCodec.createInputSurface();
        mediaCodec.start();
        createfile();
    }

    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Movies/test1.h264";
    private static String pathVideo = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Movies/";
    private BufferedOutputStream outputStream;
    FileOutputStream outStream;

    private void createfile() {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        Log.i(TAG,"路径"+path);
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private void StopEncoder() {
        try {
            mediaMuxer.stop();
            mediaMuxer.release();
            mediaCodec.stop();
            mediaCodec.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ByteBuffer[] inputBuffers;
    ByteBuffer[] outputBuffers;

    public boolean isRuning = false;

    public void StopThread() {
        isRuning = false;
        try {
            StopEncoder();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    int count = 0;
    private MediaMuxer  mediaMuxer;
    private int trackVideoIndex;

    public void StartEncoderThread() {
        Thread EncoderThread = new Thread(new Runnable() {

            @SuppressLint("NewApi")
            @Override
            public void run() {
                isRuning = true;
                byte[] input = null;
                long pts = 0;
                long generateIndex = 0;
                try {
                    mediaMuxer = new MediaMuxer(pathVideo + "/output1.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (isRuning) {
                    if (ActivityTestCamera.YUVQueue.size() > 0) {
                        input = ActivityTestCamera.YUVQueue.poll();
                        byte[] yuv420sp = new byte[m_width * m_height * 3 / 2];
                        NV21ToNV12(input, yuv420sp, m_width, m_height);
                        input = yuv420sp;
                    }
                    if (input != null) {
                        try {
                            long startMs = System.currentTimeMillis();

                            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
//                            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
                            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
                            if (inputBufferIndex >= 0) {
                                pts = computePresentationTime(generateIndex);
                                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                                inputBuffer.clear();
                                inputBuffer.put(input);
                                mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
                                generateIndex += 1;
                            }

                            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                            drain(bufferInfo);
//                            while (true) {
//                                int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
//                                if(outputBufferIndex==MediaCodec.INFO_TRY_AGAIN_LATER){
//                                    break;
//                                }
//
//                                //Log.i("AvcEncoder", "Get H264 Buffer Success! flag = "+bufferInfo.flags+",pts = "+bufferInfo.presentationTimeUs+"");
//                                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
//                                byte[] outData = new byte[bufferInfo.size];
//                                outputBuffer.put(outData);
//                                if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
//                                    configbyte = new byte[bufferInfo.size];
//                                    configbyte = outData;
//                                } else if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_SYNC_FRAME) {
//                                    byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
//                                    System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
//                                    System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
//
//                                    outputStream.write(keyframe, 0, keyframe.length);
//                                    mediaMuxer.writeSampleData(trackVideoIndex,outputBuffer,bufferInfo);
//                                } else {
//                                    outputStream.write(outData, 0, outData.length);
//                                    mediaMuxer.writeSampleData(trackVideoIndex,outputBuffer,bufferInfo);
//                                }
//
//                                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
//                                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
//                            }

                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        EncoderThread.start();

    }

    private void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / m_framerate;
    }

    private long prevOutputPTSUs = 0;
    private Boolean mMuxerStarted;

    protected long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        // presentationTimeUs should be monotonic
        // otherwise muxer fail to write
        if (result < prevOutputPTSUs)
            result = (prevOutputPTSUs - result) + result;
        return result;
    }

    /**
     * drain encoded data and write them to muxer
     */
    protected void drain(MediaCodec.BufferInfo bufferInfo) {
        if (mediaCodec == null) return;
        ByteBuffer[] encoderOutputBuffers = mediaCodec.getOutputBuffers();
        int encoderStatus, count = 0;
        LOOP:
        while (isRuning) {
            // get encoded data with maximum timeout duration of TIMEOUT_USEC(=10[msec])
            encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
            Log.i(TAG,"状态="+encoderStatus);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {

            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                if (true) Log.v(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                // this shoud not come when encoding
                encoderOutputBuffers = mediaCodec.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (true) Log.v(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
                // this status indicate the output format of codec is changed
                // this should come only once before actual encoded data
                // but this status never come on Android4.3 or less
                // and in that case, you should treat when MediaCodec.BUFFER_FLAG_CODEC_CONFIG come.
                if (mMuxerStarted) {    // second time request is error
                    throw new RuntimeException("format changed twice");
                }
                // get output format from codec and pass them to muxer
                // getOutputFormat should be called after INFO_OUTPUT_FORMAT_CHANGED otherwise crash.
                final MediaFormat format = mediaCodec.getOutputFormat(); // API >= 16
                trackVideoIndex = mediaMuxer.addTrack(format);
                mMuxerStarted = true;
                mediaMuxer.start();
                Log.i(TAG,"MediaMuxer Start");
            } else if (encoderStatus < 0) {
                // unexpected status
                if (true)
                    Log.w(TAG, "drain:unexpected result from encoder#dequeueOutputBuffer: " + encoderStatus);
            } else {
                final ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    // this never should come...may be a MediaCodec internal error
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
                }
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // You shoud set output format to muxer here when you target Android4.3 or less
                    // but MediaCodec#getOutputFormat can not call here(because INFO_OUTPUT_FORMAT_CHANGED don't come yet)
                    // therefor we should expand and prepare output format from buffer data.
                    // This sample is for API>=18(>=Android 4.3), just ignore this flag here
                    if (true) Log.d(TAG, "drain:BUFFER_FLAG_CODEC_CONFIG");
                    bufferInfo.size = 0;
                }

                if (bufferInfo.size != 0) {
                    // encoded data is ready, clear waiting counter
                    count = 0;
                    if (!mMuxerStarted) {
                        // muxer is not ready...this will prrograming failure.
                        throw new RuntimeException("drain:muxer hasn't started");
                    }
                    // write encoded data to muxer(need to adjust presentationTimeUs.
                    Log.i(TAG,"执行到这里了BUFFER_FLAG_CODEC_CONFIG");
                    bufferInfo.presentationTimeUs = getPTSUs();
                    mediaMuxer.writeSampleData(trackVideoIndex, encodedData, bufferInfo);
                    prevOutputPTSUs = bufferInfo.presentationTimeUs;
                }
                // return buffer to encoder
                mediaCodec.releaseOutputBuffer(encoderStatus, false);
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    // when EOS come.
                    isRuning = false;
                    break;      // out of while
                }
            }
        }
    }
}