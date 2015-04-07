package nl.senseos.mytimeatsense.gui.activities;

import nl.senseos.mytimeatsense.R;
import nl.senseos.mytimeatsense.bluetooth.BleAlarmReceiver;
import nl.senseos.mytimeatsense.commonsense.MsgHandler;
import nl.senseos.mytimeatsense.util.Constants;
import nl.senseos.mytimeatsense.util.Constants.Auth;
import nl.senseos.mytimeatsense.util.Constants.Sensors;
import nl.senseos.mytimeatsense.storage.DBHelper;
import nl.senseos.mytimeatsense.sync.GlobalUpdateAlarmReceiver;
import nl.senseos.mytimeatsense.sync.LocalUpdateService;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PersonalOverviewActivity extends Activity {

	private String TAG = "perosnalOverviewActivity";
	private final int REQUEST_ENABLE_BT = 10;
	private final int REQUEST_CREDENTIALS = 20;

	private TextView status;

	public static final int REPEAT_INTEVAL_MINS_BLE = 1;
	public static final int REPEAT_INTEVAL_MINS_UPLOAD = 5;

    public static final String STATUS_PRESENT = "Status: in the office";
    public static final String STATUS_ABSENT = "Status: not in the office";

	private AlarmManager alarmMgr;
	private SharedPreferences statusPrefs;

	private SharedPreferences sAuthPrefs;
	private String mUsername;
	private String mPassword;

	private Intent BleServiceIntent;
	private Intent GlobalUpdateServiceIntent;
	private PendingIntent GlobalUpdatePendingIntent;
	private PendingIntent BlePendingIntent;

    private StatusHandler mStatusHandler;

	private Handler logoutHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_overview);

		Log.e(TAG,"REPEAT_INTERVAL_MINS_BLE: "
                + REPEAT_INTEVAL_MINS_BLE+"TIME_OUT_LIMIT: "
                +LocalUpdateService.TIME_OUT_LIMIT);
	
		// check if password/username is set
		if (null == sAuthPrefs) {
			sAuthPrefs = getSharedPreferences(Auth.PREFS_CREDS,
					Context.MODE_PRIVATE);
		}

		mUsername = sAuthPrefs.getString(Auth.PREFS_CREDS_UNAME, null);
		mPassword = sAuthPrefs.getString(Auth.PREFS_CREDS_PASSWORD, null);

		if (mUsername == null || mPassword == null) {
			Intent requestCredsIntent = new Intent(this, LoginActivity.class);
			startActivityForResult(requestCredsIntent, REQUEST_CREDENTIALS);
			return;
		}
		
		// Use this check to determine whether BLE is supported on the device.
		// Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG)
					.show();
			finish();
		}
		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}

		// Ensures Bluetooth is available on the device and it is enabled. If
		// not,
		// displays a dialog requesting user permission to enable Bluetooth.
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		statusPrefs = getSharedPreferences(Constants.Status.PREFS_STATUS,
				Context.MODE_PRIVATE);

		setUpdateTimers();

	}

    public void setUpdateTimers(){

        BleServiceIntent = new Intent(this, BleAlarmReceiver.class);
        BlePendingIntent = PendingIntent.getBroadcast(this, 2,
                BleServiceIntent, 0);

        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + (1 * 1000),
                REPEAT_INTEVAL_MINS_BLE * 60 * 1000, BlePendingIntent);

        GlobalUpdateServiceIntent = new Intent(this,
                GlobalUpdateAlarmReceiver.class);
        GlobalUpdatePendingIntent = PendingIntent.getBroadcast(this, 2,
                GlobalUpdateServiceIntent, 0);

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + (1 * 1000),
                REPEAT_INTEVAL_MINS_UPLOAD * 60 * 1000,
                GlobalUpdatePendingIntent);

    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		if (requestCode == REQUEST_CREDENTIALS
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}

        setUpdateTimers();

		statusPrefs = getSharedPreferences(Constants.Status.PREFS_STATUS,
				Context.MODE_PRIVATE);

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.personal_overview, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

        if (id == R.id.personal_overview_to_light_settings) {
            toLightSettings();
            return true;
        }
		if (id == R.id.personal_overview_switch) {
			Intent requestCredsIntent = new Intent(this, LoginActivity.class);
			startActivityForResult(requestCredsIntent, REQUEST_CREDENTIALS);
			return true;
		}
		if (id == R.id.personal_overview_logout) {

			MsgHandler messageThread = MsgHandler.getInstance();
			logoutHandler = new Handler(){
				
				@Override
				public void handleMessage(Message msg){
					onLogout();
				}
			};
			
			new Handler(messageThread.getLooper()).post(new Runnable(){

				@Override
				public void run() {
					
					Editor authEditor = sAuthPrefs.edit();
					authEditor.putString(Auth.PREFS_CREDS_UNAME, null);
					authEditor.putString(Auth.PREFS_CREDS_PASSWORD, null);
					authEditor.commit();

					SharedPreferences sSensorPrefs = getSharedPreferences(
							Sensors.PREFS_SENSORS, Context.MODE_PRIVATE);
					Editor sensorEditor = sSensorPrefs.edit();
					sensorEditor.putLong(Sensors.SENSOR_LIST_COMPLETE_TIME, 0);
					sensorEditor.putString(Sensors.SENSOR_LIST_COMPLETE, null);
					sensorEditor.putString(Sensors.BEACON_SENSOR, null);
					sensorEditor.commit();

					SharedPreferences sStatusPrefs = getSharedPreferences(
							Constants.Status.PREFS_STATUS, Context.MODE_PRIVATE);
					Editor statusEditor = sStatusPrefs.edit();
                    statusEditor.putInt(Constants.Status.STATUS_DEVICE_KEY, 0);
					statusEditor.putLong(Constants.Status.STATUS_TIME_OFFICE, 0);
					statusEditor.putLong(Constants.Status.STATUS_TIME_BIKE, 0);
					statusEditor.commit();

					DBHelper DB = DBHelper.getDBHelper(PersonalOverviewActivity.this);
					DB.deleteAllRows(DBHelper.DetectionTable.TABLE_NAME);
                    DB.deleteAllRows(DBHelper.BeaconTable.TABLE_NAME);

					Message m = Message.obtain();
					logoutHandler.sendMessage(m);
				}			
			});
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void onLogout() {
		
		alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmMgr.cancel(BlePendingIntent);
		alarmMgr.cancel(GlobalUpdatePendingIntent);

		Toast.makeText(this, "Logged out successfully",
				Toast.LENGTH_LONG).show();

		Intent requestCredsIntent = new Intent(this, LoginActivity.class);
		startActivityForResult(requestCredsIntent, REQUEST_CREDENTIALS);
	}

	@Override
	public void onResume() {
		super.onResume(); // Always call the superclass method first

		statusPrefs = getSharedPreferences(Constants.Status.PREFS_STATUS,
				Context.MODE_PRIVATE);
		
		status = (TextView) findViewById(R.id.personal_overview_status);

        ImageView[] images = new ImageView[5];
        images[0] = (ImageView) findViewById(R.id.desk_1);
        images[1] = (ImageView) findViewById(R.id.desk_2);
        images[2] = (ImageView) findViewById(R.id.desk_3);
        images[3] = (ImageView) findViewById(R.id.desk_4);
        images[4] = (ImageView) findViewById(R.id.bike);

        mStatusHandler = new StatusHandler(images);

        timerHandler.postDelayed(updateTimerThread, 0);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Another activity is taking focus (this activity is about to be
		// "paused").
		timerHandler.removeCallbacks(updateTimerThread);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// The activity is no longer visible (it is now "stopped")
		timerHandler.removeCallbacks(updateTimerThread);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// The activity is about to be destroyed.
		timerHandler.removeCallbacks(updateTimerThread);
	}

    public void toLightSettings(){

        Intent intent = new Intent(this, LightSettingsActivity.class);
        startActivity(intent);
    }

	private Handler timerHandler = new Handler();

	private Runnable updateTimerThread = new Runnable() {

        int key;

		public void run() {

            key = statusPrefs.getInt(Constants.Status.STATUS_DEVICE_KEY,0);
            mStatusHandler.setImage(key);

			timerHandler.postDelayed(this, 1000);
		}
	};

    private class StatusHandler{

        ImageView[] images;
        int index=-1;

        public StatusHandler(ImageView[] im){
            images = im;
        }

        public void setImage(int key){

            if(index!=-1){
                images[index].setAlpha((float) 0.25);
            }
            index = key-1;
            if(index!=-1){
                images[index].setAlpha((float) 1);
            }
        }
    }
}
