package com.korn.im.metronome.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import com.korn.im.metronome.R;
import com.korn.im.metronome.services.ActionService;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_BPM_VALUE = "bpm_val";
    public static final String EXTRA_SOUND_STATUS = "s_status";
    public static final String EXTRA_VIBRATION_STATUS = "v_status";
    public static final String EXTRA_FLASHLIGHT_STATUS = "f_status";

    private static final int MAX_VALUE = 200;
    private static final int MIN_VALUE = 80;

    // UI elements
    private EditText mBpmValueEditText;
    private SeekBar mBpmSeekBar;

    private Intent mServiceIntent;
    private ComponentName mServiceData = null;

    private int mBpm = MIN_VALUE;
    private boolean mIsFlashlightEnabled = true;
    private boolean mIsSoundEnabled = true;
    private boolean mIsVibrationEnabled = true;

    private static ToggleButton mIndicator;
    private static IndicatorLightOperator mIndicatorOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mServiceIntent = new Intent(MainActivity.this, ActionService.class);
        initiateUI();
    }

    private void initiateUI() {
        mIndicator = (ToggleButton) findViewById(R.id.indicator);

        ((ToggleButton) findViewById(R.id.flashSelectorBtn)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsFlashlightEnabled = isChecked;
            }
        });

        ((ToggleButton) findViewById(R.id.soundSelectorBtn)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsSoundEnabled = isChecked;
            }
        });

        ((ToggleButton) findViewById(R.id.vibrationSelectorBtn)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsVibrationEnabled = isChecked;
            }
        });

        mBpmValueEditText = (EditText) findViewById(R.id.bpmValueEditText);
        mBpmSeekBar = (SeekBar) findViewById(R.id.bpmSeekBar);

        mBpmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mBpm = progress + MIN_VALUE;
                    updateProgress();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mBpmValueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().matches("\\d+")) {
                    mBpmValueEditText.setError(null);
                    mBpm = Integer.valueOf(s.toString());

                    if (mBpm <= 0) mBpm = MIN_VALUE;
                    if (mBpm > MAX_VALUE + MIN_VALUE - 1) mBpm = MAX_VALUE;

                    updateProgress();
                } else mBpmValueEditText.setError("Only numbers allowed");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        final Button btn = (Button) findViewById(R.id.startActionBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mServiceData == null) {
                    mServiceIntent.putExtra(EXTRA_BPM_VALUE, mBpm);
                    mServiceIntent.putExtra(EXTRA_FLASHLIGHT_STATUS, mIsFlashlightEnabled);
                    mServiceIntent.putExtra(EXTRA_SOUND_STATUS, mIsSoundEnabled);
                    mServiceIntent.putExtra(EXTRA_VIBRATION_STATUS, mIsVibrationEnabled);
                    startIndicator();
                    mServiceData = startService(mServiceIntent);

                    btn.setText(getString(R.string.stopActionBtnMsg));

                } else {
                    stopIndicator();
                    stopService(mServiceIntent);
                    mServiceData = null;

                    btn.setText(getString(R.string.startActionBtnMsg));
                }
            }
        });

        updateProgress();
    }

    private void startIndicator() {
        if(mIndicatorOperator == null) {
            mIndicatorOperator = new IndicatorLightOperator();
            mIndicatorOperator.execute(mBpm);
        }
    }

    private void stopIndicator() {
        if(mIndicatorOperator != null) {
            mIndicatorOperator.cancel(false);
            mIndicator.setChecked(true);
        }
    }

    private void updateProgress() {
        if(mBpmSeekBar.getProgress() != mBpm - MIN_VALUE)
            mBpmSeekBar.setProgress(mBpm - MIN_VALUE);

        if (!mBpmValueEditText.getText().toString().equals(mBpm + ""))
            mBpmValueEditText.setText(mBpm + "");
    }

    private class IndicatorLightOperator extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            int delay = (60 * 1000) / params[0];
            while (!isCancelled()) {
                try {
                    publishProgress();
                    Thread.sleep(100);
                    publishProgress();

                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            mIndicator.setChecked(!mIndicator.isChecked());
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mIndicatorOperator = null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mIndicatorOperator = null;
        }
    }
}
