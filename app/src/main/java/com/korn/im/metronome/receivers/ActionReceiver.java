package com.korn.im.metronome.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.korn.im.metronome.activitys.MainActivity;

public class ActionReceiver extends BroadcastReceiver {
    private ActionListener mActionListener;

    public ActionReceiver() {
    }

    public ActionReceiver(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(MainActivity.ACTION_ACTION) && mActionListener != null)
            mActionListener.onAction();
    }

    public interface ActionListener {
        void onAction();
    }
}
