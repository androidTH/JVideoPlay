//package com.example.min.jvideoplay.encoder;
//
//import android.util.Log;
//
///**
// * Created by min on 2016/8/30.
// */
//public class CameraRecordingStream {
//    private static final String TAG = "CameraRecordingStream";
//        private static final boolean VERBOSE = Log.isLoggable(TAG, Log.VERBOSE);
//        private static final int STREAM_STATE_IDLE = 0;
//       private static final int STREAM_STATE_CONFIGURED = 1;
//    private static final int STREAM_STATE_RECORDING = 2;
//    private static final String MIME_TYPE = "video/avc"; // H.264 AVC encoding
//    private static final int FRAME_RATE = 30; // 30fps
//    private static final int IFRAME_INTERVAL = 1; // 1 seconds between I-frames
//    private static final int TIMEOUT_USEC = 10000; // Timeout value 10ms.
//        // Sync object to protect stream state access from multiple threads.
//    private final Object mStateLock = new Object();
//
//                private int mStreamState = STREAM_STATE_IDLE;
//    private MediaCodec mEncoder;
//    private Surface mRecordingSurface;
//    private int mEncBitRate;
//    private MediaCodec.BufferInfo mBufferInfo;
//    private MediaMuxer mMuxer;
//        private int mTrackIndex = -1;
//        private boolean mMuxerStarted;
//        private boolean mUseMediaCodec = false;
//     private Size mStreamSize = new Size(-1, -1);
//    private Thread mRecordingThread;
//
//   public CameraRecordingStream() {
//        67    }
//    68
//            69    /**
//     70     * Configure stream with a size and encoder mode.
//     71     *
//     72     * @param size Size of recording stream.
//     73     * @param useMediaCodec The encoder for this stream to use, either MediaCodec
//     74     * or MediaRecorder.
//     75     * @param bitRate Bit rate the encoder takes.
//     76     */
//            77    public synchronized void configure(Size size, boolean useMediaCodec, int bitRate) {
//        78        if (getStreamState() == STREAM_STATE_RECORDING) {
//            79            throw new IllegalStateException(
//                    80                    "Stream can only be configured when stream is in IDLE state");
//            81        }
//        82
//        83        boolean isConfigChanged =
//                84                (!mStreamSize.equals(size)) ||
//                85                (mUseMediaCodec != useMediaCodec) ||
//                86                (mEncBitRate != bitRate);
//        87
//        88        mStreamSize = size;
//        89        mUseMediaCodec = useMediaCodec;
//        90        mEncBitRate = bitRate;
//        91
//        92        if (mUseMediaCodec) {
//            93            if (getStreamState() == STREAM_STATE_CONFIGURED) {
//                94                /**
//                 95                 * Stream is already configured, need release encoder and muxer
//                 96                 * first, then reconfigure only if configuration is changed.
//                 97                 */
//                98                if (!isConfigChanged) {
//                    99                    /**
//                     100                     * TODO: this is only the skeleton, it is tricky to
//                     101                     * implement because muxer need reconfigure always. But
//                     102                     * muxer is closely coupled with MediaCodec for now because
//                     103                     * muxer can only be started once format change callback is
//                     104                     * sent from mediacodec. We need decouple MediaCodec and
//                     105                     * Muxer for future.
//                     106                     */
//                    107                }
//                108                releaseEncoder();
//                109                releaseMuxer();
//                110                configureMediaCodecEncoder();
//                111            } else {
//                112                configureMediaCodecEncoder();
//                113            }
//            114        } else {
//            115            // TODO: implement MediaRecoder mode.
//            116            Log.w(TAG, "MediaRecorder configure is not implemented yet");
//            117        }
//        118
//        119        setStreamState(STREAM_STATE_CONFIGURED);
//        120    }
//    121
//            122    /**
//     123     * Add the stream output surface to the target output surface list.
//     124     *
//     125     * @param outputSurfaces The output surface list where the stream can
//     126     * add/remove its output surface.
//     127     * @param detach Detach the recording surface from the outputSurfaces.
//     128     */
//            129    public synchronized void onConfiguringOutputs(List<Surface> outputSurfaces,
//                                                                 130            boolean detach) {
//        131        if (detach) {
//            132            // Can detach the surface in CONFIGURED and RECORDING state
//            133            if (getStreamState() != STREAM_STATE_IDLE) {
//                134                outputSurfaces.remove(mRecordingSurface);
//                135            } else {
//                136                Log.w(TAG, "Can not detach surface when recording stream is in IDLE state");
//                137            }
//            138        } else {
//            139            // Can add surface only in CONFIGURED state.
//            140            if (getStreamState() == STREAM_STATE_CONFIGURED) {
//                141                outputSurfaces.add(mRecordingSurface);
//                142            } else {
//                143                Log.w(TAG, "Can only add surface when recording stream is in CONFIGURED state");
//                144            }
//            145        }
//        146    }
//    147
//            148    /**
//     149     * Update capture request with configuration required for recording stream.
//     150     *
//     151     * @param requestBuilder Capture request builder that needs to be updated
//     152     * for recording specific camera settings.
//     153     * @param detach Detach the recording surface from the capture request.
//     154     */
//            155    public synchronized void onConfiguringRequest(CaptureRequest.Builder requestBuilder,
//                                                                 156            boolean detach) {
//        157        if (detach) {
//            158            // Can detach the surface in CONFIGURED and RECORDING state
//            159            if (getStreamState() != STREAM_STATE_IDLE) {
//                160                requestBuilder.removeTarget(mRecordingSurface);
//                161            } else {
//                162                Log.w(TAG, "Can not detach surface when recording stream is in IDLE state");
//                163            }
//            164        } else {
//            165            // Can add surface only in CONFIGURED state.
//            166            if (getStreamState() == STREAM_STATE_CONFIGURED) {
//                167                requestBuilder.addTarget(mRecordingSurface);
//                168            } else {
//                169                Log.w(TAG, "Can only add surface when recording stream is in CONFIGURED state");
//                170            }
//            171        }
//        172    }
//    173
//            174    /**
//     175     * Start recording stream. Calling start on an already started stream has no
//     176     * effect.
//     177     */
//            178    public synchronized void start() {
//        179        if (getStreamState() == STREAM_STATE_RECORDING) {
//            180            Log.w(TAG, "Recording stream is already started");
//            181            return;
//            182        }
//        183
//        184        if (getStreamState() != STREAM_STATE_CONFIGURED) {
//            185            throw new IllegalStateException("Recording stream is not configured yet");
//            186        }
//        187
//        188        if (mUseMediaCodec) {
//            189            setStreamState(STREAM_STATE_RECORDING);
//            190            startMediaCodecRecording();
//            191        } else {
//            192            // TODO: Implement MediaRecorder mode recording
//            193            Log.w(TAG, "MediaRecorder mode recording is not implemented yet");
//            194        }
//        195    }
//    196
//            197    /**
//     198     * <p>
//     199     * Stop recording stream. Calling stop on an already stopped stream has no
//     200     * effect. Producer(in this case, CameraDevice) should stop before this call
//     201     * to avoid sending buffers to a stopped encoder.
//     202     * </p>
//     203     * <p>
//     204     * TODO: We have to release encoder and muxer for MediaCodec mode because
//     205     * encoder is closely coupled with muxer, and muxser can not be reused
//     206     * across different recording session(by design, you can not reset/restart
//     207     * it). To save the subsequent start recording time, we need avoid releasing
//     208     * encoder for future.
//     209     * </p>
//     210     */
//            211    public synchronized void stop() {
//        212        if (getStreamState() != STREAM_STATE_RECORDING) {
//            213            Log.w(TAG, "Recording stream is not started yet");
//            214            return;
//            215        }
//        216
//        217        setStreamState(STREAM_STATE_IDLE);
//        218        Log.e(TAG, "setting camera to idle");
//        219        if (mUseMediaCodec) {
//            220            // Wait until recording thread stop
//            221            try {
//                222                mRecordingThread.join();
//                223            } catch (InterruptedException e) {
//                224                         throw new RuntimeException("Stop recording failed", e);
//                225            }
//            226            // Drain encoder
//            227            doMediaCodecEncoding(/* notifyEndOfStream */true);
//            228            releaseEncoder();
//            229            releaseMuxer();
//            230        } else {
//            231            // TODO: implement MediaRecorder mode recording stop.
//            232            Log.w(TAG, "MediaRecorder mode recording stop is not implemented yet");
//            233        }
//        234    }
//    235
//            236    /**
//     237     * Starts MediaCodec mode recording.
//     238     */
//            239    private void startMediaCodecRecording() {
//        240        /**
//         241         * Start video recording asynchronously. we need a loop to handle output
//         242         * data for each frame.
//         243         */
//        244        mRecordingThread = new Thread() {
//            245            @Override
//            246            public void run() {
//                247                if (VERBOSE) {
//                    248                    Log.v(TAG, "Recording thread starts");
//                    249                }
//                250
//                251                while (getStreamState() == STREAM_STATE_RECORDING) {
//                    252                    // Feed encoder output into the muxer until recording stops.
//                    253                    doMediaCodecEncoding(/* notifyEndOfStream */false);
//                    254                }
//                255                if (VERBOSE) {
//                    256                    Log.v(TAG, "Recording thread completes");
//                    257                }
//                258                return;
//                259            }
//            260        };
//        261        mRecordingThread.start();
//        262    }
//    263
//            264    // Thread-safe access to the stream state.
//            265    private synchronized void setStreamState(int state) {
//        266        synchronized (mStateLock) {
//            267            if (state < STREAM_STATE_IDLE) {
//                268                throw new IllegalStateException("try to set an invalid state");
//                269            }
//            270            mStreamState = state;
//            271        }
//        272    }
//    273
//            274    // Thread-safe access to the stream state.
//            275    private int getStreamState() {
//        276        synchronized(mStateLock) {
//            277            return mStreamState;
//            278        }
//        279    }
//    280
//            281    private void releaseEncoder() {
//        282        // Release encoder
//        283        if (VERBOSE) {
//            284            Log.v(TAG, "releasing encoder");
//            285        }
//        286        if (mEncoder != null) {
//            287            mEncoder.stop();
//            288            mEncoder.release();
//            289            if (mRecordingSurface != null) {
//                290                mRecordingSurface.release();
//                291            }
//            292            mEncoder = null;
//            293        }
//        294    }
//    295
//            296    private void releaseMuxer() {
//        297        if (VERBOSE) {
//            298            Log.v(TAG, "releasing muxer");
//            299        }
//        300
//        301        if (mMuxer != null) {
//            302            mMuxer.stop();
//            303            mMuxer.release();
//            304            mMuxer = null;
//            305        }
//        306    }
//    307
//            308    private String getOutputMediaFileName() {
//        309        String state = Environment.getExternalStorageState();
//        310        // Check if external storage is mounted
//        311        if (!Environment.MEDIA_MOUNTED.equals(state)) {
//            312            Log.e(TAG, "External storage is not mounted!");
//            313            return null;
//            314        }
//        315
//        316        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
//                317                Environment.DIRECTORY_DCIM), "TestingCamera2");
//        318        // Create the storage directory if it does not exist
//        319        if (!mediaStorageDir.exists()) {
//            320            if (!mediaStorageDir.mkdirs()) {
//                321                Log.e(TAG, "Failed to create directory " + mediaStorageDir.getPath()
//                        322                        + " for pictures/video!");
//                323                return null;
//                324            }
//            325        }
//        326
//        327        // Create a media file name
//        328        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        329        String mediaFileName = mediaStorageDir.getPath() + File.separator +
//                330                "VID_" + timeStamp + ".mp4";
//        331
//        332        return mediaFileName;
//        333    }
//    334
//            335    /**
//     336     * Configures encoder and muxer state, and prepares the input Surface.
//     337     * Initializes mEncoder, mMuxer, mRecordingSurface, mBufferInfo,
//     338     * mTrackIndex, and mMuxerStarted.
//     339     */
//            340    private void configureMediaCodecEncoder() {
//        341        mBufferInfo = new MediaCodec.BufferInfo();
//        342        MediaFormat format =
//                343                MediaFormat.createVideoFormat(MIME_TYPE,
//                344                        mStreamSize.getWidth(), mStreamSize.getHeight());
//        345        /**
//         346         * Set encoding properties. Failing to specify some of these can cause
//         347         * the MediaCodec configure() call to throw an exception.
//         348         */
//        349        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
//                350                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
//        351        format.setInteger(MediaFormat.KEY_BIT_RATE, mEncBitRate);
//        352        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
//        353        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
//        354        Log.i(TAG, "configure video encoding format: " + format);
//        355
//        356        // Create/configure a MediaCodec encoder.
//        357        mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
//        358        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//        359        mRecordingSurface = mEncoder.createInputSurface();
//        360        mEncoder.start();
//        361
//        362        String outputFileName = getOutputMediaFileName();
//        363        if (outputFileName == null) {
//            364            throw new IllegalStateException("Failed to get video output file");
//            365        }
//        366
//        367        /**
//         368         * Create a MediaMuxer. We can't add the video track and start() the
//         369         * muxer until the encoder starts and notifies the new media format.
//         370         */
//        371        try {
//            372            mMuxer = new MediaMuxer(
//                    373                    outputFileName, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//            374        } catch (IOException ioe) {
//            375            throw new IllegalStateException("MediaMuxer creation failed", ioe);
//            376        }
//        377        mMuxerStarted = false;
//        378    }
//    379
//            380    /**
//     381     * Do encoding by using MediaCodec encoder, then extracts all pending data
//     382     * from the encoder and forwards it to the muxer.
//     383     * <p>
//     384     * If notifyEndOfStream is not set, this returns when there is no more data
//     385     * to output. If it is set, we send EOS to the encoder, and then iterate
//     386     * until we see EOS on the output. Calling this with notifyEndOfStream set
//     387     * should be done once, before stopping the muxer.
//     388     * </p>
//     389     * <p>
//     390     * We're just using the muxer to get a .mp4 file and audio is not included
//     391     * here.
//     392     * </p>
//     393     */
//            394    private void doMediaCodecEncoding(boolean notifyEndOfStream) {
//        395        if (VERBOSE) {
//            396            Log.v(TAG, "doMediaCodecEncoding(" + notifyEndOfStream + ")");
//            397        }
//        398
//        399        if (notifyEndOfStream) {
//            400            mEncoder.signalEndOfInputStream();
//            401        }
//        402
//        403        ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();
//        404        boolean notDone = true;
//        405        while (notDone) {
//            406            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
//            407            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
//                408                if (!notifyEndOfStream) {
//                    409                    /**
//                     410                     * Break out of the while loop because the encoder is not
//                     411                     * ready to output anything yet.
//                     412                     */
//                    413                    notDone = false;
//                    414                } else {
//                    415                    if (VERBOSE) {
//                        416                        Log.v(TAG, "no output available, spinning to await EOS");
//                        417                    }
//                    418                }
//                419            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                420                // generic case for mediacodec, not likely occurs for encoder.
//                421                encoderOutputBuffers = mEncoder.getOutputBuffers();
//                422            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                423                /**
//                 424                 * should happen before receiving buffers, and should only
//                 425                 * happen once
//                 426                 */
//                427                if (mMuxerStarted) {
//                    428                    throw new IllegalStateException("format changed twice");
//                    429                }
//                430                MediaFormat newFormat = mEncoder.getOutputFormat();
//                431                if (VERBOSE) {
//                    432                    Log.v(TAG, "encoder output format changed: " + newFormat);
//                    433                }
//                434                mTrackIndex = mMuxer.addTrack(newFormat);
//                435                mMuxer.start();
//                436                mMuxerStarted = true;
//                437            } else if (encoderStatus < 0) {
//                438                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
//                439            } else {
//                440                // Normal flow: get output encoded buffer, send to muxer.
//                441                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
//                442                if (encodedData == null) {
//                    443                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
//                            444                            " was null");
//                    445                }
//                446
//                447                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//                    448                    /**
//                     449                     * The codec config data was pulled out and fed to the muxer
//                     450                     * when we got the INFO_OUTPUT_FORMAT_CHANGED status. Ignore
//                     451                     * it.
//                     452                     */
//                    453                    if (VERBOSE) {
//                        454                        Log.v(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
//                        455                    }
//                    456                    mBufferInfo.size = 0;
//                    457                }
//                458
//                459                if (mBufferInfo.size != 0) {
//                    460                    if (!mMuxerStarted) {
//                        461                        throw new RuntimeException("muxer hasn't started");
//                        462                    }
//                    463
//                    464                    /**
//                     465                     * It's usually necessary to adjust the ByteBuffer values to
//                     466                     * match BufferInfo.
//                     467                     */
//                    468                    encodedData.position(mBufferInfo.offset);
//                    469                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
//                    470
//                    471                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
//                    472                    if (VERBOSE) {
//                        473                        Log.v(TAG, "sent " + mBufferInfo.size + " bytes to muxer");
//                        474                    }
//                    475                }
//                476
//                477                mEncoder.releaseOutputBuffer(encoderStatus, false);
//                478
//                479                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                    480                    if (!notifyEndOfStream) {
//                        481                        Log.w(TAG, "reached end of stream unexpectedly");
//                        482                    } else {
//                        483                        if (VERBOSE) {
//                            484                            Log.v(TAG, "end of stream reached");
//                            485                        }
//                        486                    }
//                    487                    // Finish encoding.
//                    488                    notDone = false;
//                    489                }
//                490            }
//            491        } // End of while(notDone)
//        492    }
//}
