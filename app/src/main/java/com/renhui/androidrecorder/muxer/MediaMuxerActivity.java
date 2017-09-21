package com.renhui.androidrecorder.muxer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.renhui.androidrecorder.R;

import java.io.IOException;

/**
 * 音视频混合界面
 */
public class MediaMuxerActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    SurfaceView surfaceView;
    Button startStopButton;

    Camera camera;
    SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media_muxer);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "申请权限", Toast.LENGTH_SHORT).show();
            // 申请 相机 麦克风权限
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }

        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        startStopButton = (Button) findViewById(R.id.startStop);

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag().toString().equalsIgnoreCase("stop")) {
                    stopCamera();
                    view.setTag("start");
                    ((TextView) view).setText("开始");
                } else {
                    startCamera();
                    view.setTag("stop");
                    ((TextView) view).setText("停止");
                }
            }
        });

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        if (supportH264Codec()) {
            Log.e("MainActivity", "support H264 hard codec");
        } else {
            Log.e("MainActivity", "not support H264 hard codec");
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.w("MainActivity", "enter surfaceCreated method");
        this.surfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.w("MainActivity", "enter surfaceChanged method");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.w("MainActivity", "enter surfaceDestroyed method");
        stopCamera();

    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        Log.e("111", "bytes" + bytes.length);
    }

    //----------------------- MediaCodec 相关 ------------------------------------

    /**
     * 检测是否支持H264编码格式
     */
    private boolean supportH264Codec() {
        // 遍历支持的编码格式信息
        if (Build.VERSION.SDK_INT >= 18) {
            for (int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);

                String[] types = codecInfo.getSupportedTypes();
                for (int i = 0; i < types.length; i++) {
                    if (types[i].equalsIgnoreCase("video/avc")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    //----------------------- 摄像头操作相关 --------------------------------------

    /**
     * 打开摄像头
     */
    private void startCamera() {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        camera.setDisplayOrientation(90);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPreviewSize(1280, 720);

        try {
            camera.setParameters(parameters);
            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(MediaMuxerActivity.this);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭摄像头
     */
    private void stopCamera() {
        // 停止预览并释放资源
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera = null;
        }
    }


}