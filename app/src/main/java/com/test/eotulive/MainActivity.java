package com.test.eotulive;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import net.ossrs.yasea.SrsCameraView;

import net.ossrs.yasea.SrsEncoder;
import net.ossrs.yasea.SrsMp4Muxer;
import net.ossrs.yasea.SrsPublisher;

import net.ossrs.yasea.rtmp.RtmpPublisher;

import java.util.Random;

public class MainActivity extends Activity {
    private static final String TAG = "Yasea";

    Button btnPublish = null;
    Button btnSwitchCamera = null;
    Button btnRecord = null;
    Button btnSwitchEncoder = null;

    private SharedPreferences sp;
    private String rtmpUrl = "rtmp://w.gslb.lecloud.com/live/2016101230000009e99";// "rtmp://0.0.0.0/" + getRandomAlphaString(3) + '/' + getRandomAlphaDigitString(5);
    private String recPath = Environment.getExternalStorageDirectory().getPath() + "/test.mp4";

    private SrsPublisher mPublisher;
    private SrsCameraView mSrsCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        // restore data.
        sp = getSharedPreferences("Yasea2", MODE_PRIVATE);
        rtmpUrl = sp.getString("rtmpUrl", rtmpUrl);

        // initialize url.
        final EditText efu = (EditText) findViewById(R.id.url);
        efu.setText(rtmpUrl);

        btnPublish = (Button) findViewById(R.id.publish);
        btnSwitchCamera = (Button) findViewById(R.id.swCam);
        btnRecord = (Button) findViewById(R.id.record);
        btnSwitchEncoder = (Button) findViewById(R.id.swEnc);
        mSrsCameraView = (SrsCameraView) findViewById(R.id.preview);
        mPublisher = new SrsPublisher(this);
        mPublisher.setScreenOrientation(Configuration.ORIENTATION_LANDSCAPE);
        mSrsCameraView.setPreviewResolution(mPublisher.getPreviewWidth(), mPublisher.getPreviewHeight());//大小
        mSrsCameraView.setPreviewRotation(0);//旋转
        mSrsCameraView.setPreviewCallback(mPublisher);


        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnPublish.getText().toString().contentEquals("publish")) {
                    rtmpUrl = efu.getText().toString();
                    Log.i(TAG, String.format("RTMP URL changed to %s", rtmpUrl));
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("rtmpUrl", rtmpUrl);
                    editor.apply();

                    mPublisher.setPreviewResolution(1280, 720);
                    mPublisher.setOutputResolution(384, 640);
                    mPublisher.setVideoSmoothMode();
                    mPublisher.startPublish(rtmpUrl);

                    if (btnSwitchEncoder.getText().toString().contentEquals("soft enc")) {
                        Toast.makeText(getApplicationContext(), "Use hard encoder", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Use soft encoder", Toast.LENGTH_SHORT).show();
                    }
                    btnPublish.setText("stop");
                    btnSwitchEncoder.setEnabled(false);
                } else if (btnPublish.getText().toString().contentEquals("stop")) {
                    mPublisher.stopPublish();
                    mPublisher.stopRecord();

                    btnPublish.setText("publish");
                    btnRecord.setText("record");
                    btnSwitchEncoder.setEnabled(true);
                }
            }
        });

        btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Camera.getNumberOfCameras() > 0) {
                    int cameraId = (mSrsCameraView.getCamraId() + 1) % Camera.getNumberOfCameras();

                    mSrsCameraView.setCameraId(cameraId);
                    mSrsCameraView.stopCamera();

                    mPublisher.setCameraFace(cameraId == 0);

                    try {
                        mSrsCameraView.startCamera();
                    } catch (Exception e) {
                    }
                }
            }
        });

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnRecord.getText().toString().contentEquals("record")) {
                    mPublisher.startRecord(recPath);

                    btnRecord.setText("pause");
                } else if (btnRecord.getText().toString().contentEquals("pause")) {
                    mPublisher.pauseRecord();
                    btnRecord.setText("resume");
                } else if (btnRecord.getText().toString().contentEquals("resume")) {
                    mPublisher.resumeRecord();
                    btnRecord.setText("pause");
                }
            }
        });

        btnSwitchEncoder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnSwitchEncoder.getText().toString().contentEquals("soft enc")) {
                    mPublisher.swithToSoftEncoder();
                    btnSwitchEncoder.setText("hard enc");
                } else if (btnSwitchEncoder.getText().toString().contentEquals("hard enc")) {
                    mPublisher.swithToHardEncoder();
                    btnSwitchEncoder.setText("soft enc");
                }
            }
        });

        mPublisher.setPublishEventHandler(new RtmpPublisher.EventHandler() {
            @Override
            public void onRtmpConnecting(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onRtmpConnected(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onRtmpVideoStreaming(final String msg) {
                Log.i("", msg);
            }

            @Override
            public void onRtmpAudioStreaming(final String msg) {
                Log.i("", msg);
            }

            @Override
            public void onRtmpStopped(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onRtmpDisconnected(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onRtmpOutputFps(final double fps) {
                Log.i(TAG, String.format("Output Fps: %f", fps));
            }
        });

        mPublisher.setRecordEventHandler(new SrsMp4Muxer.EventHandler() {
            @Override
            public void onRecordPause(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onRecordResume(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onRecordStarted(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Recording file: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onRecordFinished(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "MP4 file saved: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        mPublisher.setNetworkEventHandler(new SrsEncoder.EventHandler() {
            @Override
            public void onNetworkResume(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onNetworkWeak(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                final String msg = ex.getMessage();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                        mPublisher.stopPublish();
                        mPublisher.stopRecord();
                        btnPublish.setText("publish");
                        btnRecord.setText("record");
                        btnSwitchEncoder.setEnabled(true);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Button btn = (Button) findViewById(R.id.publish);
        btn.setEnabled(true);
        mPublisher.resumeRecord();

        new Thread() {
            @Override
            public void run() {
                try {
                    // stopCamera();
                    Thread.sleep(1000);

                    if (!mSrsCameraView.isOpened()) {
                        mSrsCameraView.startCamera();
//                        EventBus.getDefault().post(
//                                new LiveEvent(LiveEvent.Camera_Success));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
//                    EventBus.getDefault().post(
//                            new LiveEvent(LiveEvent.Camera_Faild));
                }
            }
        }.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPublisher.pauseRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPublisher.stopPublish();
        mPublisher.stopRecord();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mSrsCameraView.setPreviewRotation(90);
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mSrsCameraView.setPreviewRotation(0);
        }
        mPublisher.stopEncode();
        mPublisher.stopRecord();
        btnRecord.setText("record");
        mPublisher.setScreenOrientation(newConfig.orientation);
        if (btnPublish.getText().toString().contentEquals("stop")) {
            mPublisher.startEncode();
        }
    }

    private static String getRandomAlphaString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    private static String getRandomAlphaDigitString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
}
