package nl.senseos.mytimeatsense.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GlobalUpdateAlarmReceiver extends BroadcastReceiver{

	private final static String TAG = BroadcastReceiver.class
			.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.d(TAG,"onReceive GlobalUpdate");
		Intent statusUpdateIntent = new Intent(context, StatusUpdateService.class);
		context.startService(statusUpdateIntent);

        Intent beaconUpdateIntent = new Intent(context, StatusUpdateService.class);
        context.startService(beaconUpdateIntent);
	}
}