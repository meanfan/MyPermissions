package com.mean.mypermissions.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ImplantReceiver extends BroadcastReceiver {
    public static final String ACTION = "com.mean.permissions.action.RECEIVE_COMMAND";
    public IntentFilter action;

    public ImplantReceiver() {
        super();
        action = new IntentFilter();
        action.addAction(ACTION);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
