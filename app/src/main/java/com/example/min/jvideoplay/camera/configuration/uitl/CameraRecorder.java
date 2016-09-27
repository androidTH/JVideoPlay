/**
 * Copyright 2014 Jeroen Mols
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.min.jvideoplay.camera.configuration.uitl;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Build;
import android.util.Log;


import com.example.min.jvideoplay.camera.configuration.CaptureConfiguration;

import java.io.IOException;

public class CameraRecorder {

    private static final String TAG = CameraRecorder.class.getSimpleName();
    private String mRecordPath;
    private CameraInstance mCameraInstance;
    private CaptureConfiguration mCaptureConfiguration;
    private MediaRecorder mRecorder;
    private boolean mRecording = false;
    private boolean mFrontCamera;

    public CameraRecorder(String recordPath, CameraInstance cameraInstance) {
        this.mRecordPath = recordPath;
        this.mCameraInstance = cameraInstance;
        this.mCaptureConfiguration = new CaptureConfiguration();
    }

    public CameraRecorder(String recordPath, CameraInstance mCameraInstance, CaptureConfiguration mCaptureConfiguration) {
        this.mRecordPath = recordPath;
        this.mCameraInstance = mCameraInstance;
        this.mCaptureConfiguration = mCaptureConfiguration;
    }

    public boolean toggleRecording() {
        if (isRecording()) {
            return stopRecording("toggle manual stop recording...");
        } else {
            return startRecording();
        }
    }

    protected boolean startRecording() {
        mRecording = false;

        if (!initRecorder()) {
            return mRecording;
        }
        if (!prepareRecorder()) {
            return mRecording;
        }
        if (!startRecorder()) {
            return mRecording;
        }

        mRecording = true;
        CLog.d(TAG, "Successfully started recording - outputfile: " + mRecordPath);

        return mRecording;
    }

    public boolean stopRecording(String message) {
        if (!isRecording()) {
            return false;
        }
        try {
            getMediaRecorder().stop();
            CLog.d(TAG, "Successfully stopped recording - message: " + message);
        } catch (final RuntimeException e) {
            CLog.d(TAG, "Failed to stop recording");
            return false;
        }
        mRecording = false;
        return true;
    }

    private boolean initRecorder() {
        mCameraInstance.unlockCamera();

        setMediaRecorder(new MediaRecorder()); //配置MediaRecorder —— 按照如下顺序调用MediaRecorder 中的方法
        configureMediaRecorder(getMediaRecorder(), mCameraInstance.getCameraDevice());

        CLog.d(TAG, "MediaRecorder successfully initialized");
        return true;
    }

    protected void configureMediaRecorder(final MediaRecorder recorder, android.hardware.Camera camera) throws IllegalStateException, IllegalArgumentException {
        recorder.setCamera(camera);
        Log.i(TAG,"autioSource"+mCaptureConfiguration.getAudioSource());
        recorder.setAudioSource(mCaptureConfiguration.getAudioSource());
        recorder.setVideoSource(mCaptureConfiguration.getVideoSource());

        CamcorderProfile baseProfile = getBaseRecordingProfile();
        baseProfile.fileFormat = mCaptureConfiguration.getOutputFormat();
        baseProfile.videoBitRate = mCaptureConfiguration.getVideoBitrate();
        baseProfile.videoFrameRate = 15;

        mFrontCamera = mCameraInstance.getFacing() == CameraInstance.CAMERA_FRONT;
        if (!mFrontCamera) {
            Log.i(TAG, String.format("CameraRecorder preferPreview size: %d x %d", mCameraInstance.previewWidth(), mCameraInstance.previewHeight()));
            baseProfile.videoFrameWidth = mCameraInstance.previewWidth();
            baseProfile.videoFrameHeight = mCameraInstance.previewHeight();
            baseProfile.audioCodec = mCaptureConfiguration.getAudioEncoder();
            baseProfile.videoCodec = mCaptureConfiguration.getVideoEncoder();
        }

        recorder.setProfile(baseProfile);

        recorder.setMaxDuration(mCaptureConfiguration.getMaxCaptureDuration());
        recorder.setOutputFile(mRecordPath);

        //解决前置摄像头拍出的视频,播放时上下颠倒的问题
        recorder.setOrientationHint(mFrontCamera ? mCameraInstance.getDisplayOrientation() + 180 : mCameraInstance.getDisplayOrientation());

        try {
            recorder.setMaxFileSize(mCaptureConfiguration.getMaxCaptureFileSize());
        } catch (IllegalArgumentException e) {
            CLog.e(TAG, "Failed to set max filesize - illegal argument: " + mCaptureConfiguration.getMaxCaptureFileSize());
        } catch (RuntimeException e2) {
            CLog.e(TAG, "Failed to set max filesize - runtime exception");
        }
        recorder.setOnInfoListener(new OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                switch (what) {
                    case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
                        // NOP
                        break;
                    case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                        CLog.d(TAG, "MediaRecorder max duration reached");
                        stopRecording("Capture stopped - Max duration reached");
                        break;
                    case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                        CLog.d(TAG, "MediaRecorder max filesize reached");
                        stopRecording("Capture stopped - Max file size reached");
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private boolean prepareRecorder() {
        try {
            getMediaRecorder().prepare();
            CLog.d(TAG, "MediaRecorder successfully prepared");
            return true;
        } catch (final IllegalStateException e) {
            e.printStackTrace();
            CLog.e(TAG, "MediaRecorder preparation failed - " + e.toString());
            return false;
        } catch (final IOException e) {
            e.printStackTrace();
            CLog.e(TAG, "MediaRecorder preparation failed - " + e.toString());
            return false;
        }
    }

    private boolean startRecorder() {
        try {
            getMediaRecorder().start();
            CLog.d(TAG, "MediaRecorder successfully started");
            return true;
        } catch (final IllegalStateException e) {
            e.printStackTrace();
            releaseRecorderResources();
            CLog.e(TAG, "MediaRecorder start failed - " + e.toString());
            return false;
        } catch (final RuntimeException e2) {
            e2.printStackTrace();
            releaseRecorderResources();
            CLog.e(TAG, "MediaRecorder start failed - " + e2.toString());
            return false;
        }
    }

    protected boolean isRecording() {
        return mRecording;
    }

    protected void setMediaRecorder(MediaRecorder recorder) {
        mRecorder = recorder;
    }

    protected MediaRecorder getMediaRecorder() {
        return mRecorder;
    }

    public void releaseRecorderResources() {
        MediaRecorder recorder = getMediaRecorder();
        if (recorder != null) {
            recorder.release();
            setMediaRecorder(null);
        }
    }

    public CamcorderProfile getBaseRecordingProfile() {
        CamcorderProfile returnProfile;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            returnProfile = getDefaultRecordingProfile();
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
            returnProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
            returnProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        } else {
            returnProfile = getDefaultRecordingProfile();
        }
        return returnProfile;
    }

    private CamcorderProfile getDefaultRecordingProfile() {
        CamcorderProfile highProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        if (highProfile != null) {
            return highProfile;
        }
        CamcorderProfile lowProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        if (lowProfile != null) {
            return lowProfile;
        }
        throw new RuntimeException("No quality level found");
    }
}