package com.jav.info;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AvailableNetwork extends BroadcastReceiver {
	@Override
	public void onReceive(Context context,Intent intent){
		
			Toast.makeText(context,"",Toast.LENGTH_SHORT).show();
		
	}
}