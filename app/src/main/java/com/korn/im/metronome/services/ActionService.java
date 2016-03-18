package com.korn.im.metronome.services;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;
import android.os.Vibrator;

import com.korn.im.metronome.R;
import com.korn.im.metronome.activity.MainActivity;

public class ActionService extends IntentService {
    private Vibrator mVibrator;
    private MediaPlayer mMediaPlayer;
    private Camera mCamera = null;
    private Parameters mParameters;
    private boolean mIsRunning;

    public ActionService() {
        super("ActionService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        setupVibrator(intent);
        setupFlashlight(intent);
        setupSignal(intent);

        int delay = (60 * 1000) / intent.getIntExtra(MainActivity.EXTRA_BPM_VALUE, 1) ;

        mIsRunning = true;
        while (mIsRunning) {
            if(mMediaPlayer != null)
                mMediaPlayer.start();
            if(mVibrator != null)
                mVibrator.vibrate(100);
            cameraBlink();
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupSignal(Intent intent) {
        if(!intent.getBooleanExtra(MainActivity.EXTRA_SOUND_STATUS, true)) return;

        mMediaPlayer = MediaPlayer.create(this, R.raw.sound);
        mMediaPlayer.setVolume(1f, 1f);
    }

    private void setupVibrator(Intent intent) {
        if(!intent.getBooleanExtra(MainActivity.EXTRA_VIBRATION_STATUS, true)) return;
            mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    private void setupFlashlight(Intent intent) {
        if(!intent.getBooleanExtra(MainActivity.EXTRA_FLASHLIGHT_STATUS, true))
            return;

        if((mCamera = Camera.open()) != null)
            mParameters = mCamera.getParameters();
    }

    private void cameraBlink() {
        if(mCamera == null) return;

        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(mParameters);
        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(mParameters);
    }

    @Override
    public void onDestroy() {
        mIsRunning = false;
        super.onDestroy();
        if(mCamera !=  null) {
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mParameters);
            mCamera.release();
        }
        if (mMediaPlayer != null)
            mMediaPlayer.release();
    }
}
