package com.korn.im.metronome.activitys;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.korn.im.metronome.receivers.ActionReceiver;
import com.korn.im.metronome.R;
import com.korn.im.metronome.services.ActionService;

public class MainActivity extends AppCompatActivity implements ActionReceiver.ActionListener {
    public static final String EXTRA_BPM_VALUE = "bpm_val";
    public static final String EXTRA_SOUND_STATUS = "s_status";
    public static final String EXTRA_VIBRATION_STATUS = "v_status";
    public static final String EXTRA_FLASHLIGHT_STATUS = "f_status";

    public static final String ACTION_ACTION = "com.korn.im.metronome.action";

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

    private ToggleButton mIndicator;
    private boolean mError = false;
    private ActionReceiver mActionReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mServiceIntent = new Intent(MainActivity.this, ActionService.class);
        initiateUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mActionReceiver = new ActionReceiver(this), new IntentFilter(ACTION_ACTION));
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
                    mError = false;
                    updateProgress();
                } else  mError = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        final Button btn = (Button) findViewById(R.id.startActionBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mError) {
                    mBpmValueEditText.setError("Invalid data");
                    mBpmValueEditText.requestFocus();
                    return;
                }

                if (mServiceData == null) {
                    mServiceIntent.putExtra(EXTRA_BPM_VALUE, mBpm);
                    mServiceIntent.putExtra(EXTRA_FLASHLIGHT_STATUS, mIsFlashlightEnabled);
                    mServiceIntent.putExtra(EXTRA_SOUND_STATUS, mIsSoundEnabled);
                    mServiceIntent.putExtra(EXTRA_VIBRATION_STATUS, mIsVibrationEnabled);

                    mServiceData = startService(mServiceIntent);

                    btn.setText(getString(R.string.stopActionBtnMsg));

                } else {
                    stopService(mServiceIntent);
                    mServiceData = null;

                    btn.setText(getString(R.string.startActionBtnMsg));
                }
            }
        });

        updateProgress();
    }

    private void updateProgress() {
        if(mBpmSeekBar.getProgress() != mBpm - MIN_VALUE)
            mBpmSeekBar.setProgress(mBpm - MIN_VALUE);

        if (!mBpmValueEditText.getText().toString().equals(mBpm + ""))
            mBpmValueEditText.setText(mBpm + "");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mActionReceiver != null)
        unregisterReceiver(mActionReceiver);
    }

    @Override
    public void onAction() {
        mIndicator.setChecked(!mIndicator.isChecked());
    }
}
