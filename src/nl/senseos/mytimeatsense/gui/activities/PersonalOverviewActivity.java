package nl.senseos.mytimeatsense.gui.activities;

import nl.senseos.mytimeatsense.R;
import nl.senseos.mytimeatsense.bluetooth.BleAlarmReceiver;
import nl.senseos.mytimeatsense.commonsense.MsgHandler;
import nl.senseos.mytimeatsense.util.Clock;
import nl.senseos.mytimeatsense.util.Constants.Auth;
import nl.senseos.mytimeatsense.util.Constants.Sensors;
import nl.senseos.mytimeatsense.storage.DBHelper;
import nl.senseos.mytimeatsense.sync.GlobalUpdateAlarmReceiver;
import nl.senseos.mytimeatsense.sync.LocalUpdateService;
import nl.senseos.mytimeatsense.util.Constants.StatusPrefs;
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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class PersonalOverviewActivity extends Activity {

	private String TAG = "perosnalOverviewActivity";
	private final int REQUEST_ENABLE_BT = 10;
	private final int REQUEST_CREDENTIALS = 20;

	private TextView todayHours;
	private TextView todayMinutes;
	private TextView todaySeconds;
	
	private TextView thisWeekDays;
	private TextView thisWeekHours;
	private TextView thisWeekMinutes;
	private TextView thisWeekSeconds;

	private TextView thisLifeDays;
	private TextView thisLifeHours;
	private TextView thisLifeMinutes;
	private TextView thisLifeSeconds;

	private TextView status;

	public static final long REPEAT_INTEVAL_MINS_BLE = 5;
	public static final long REPEAT_INTEVAL_MINS_UPLOAD = 31;

	private AlarmManager alarmMgr;
	private SharedPreferences statusPrefs;

	private SharedPreferences sAuthPrefs;
	private String mUsername;
	private String mPassword;

	private Intent BleServiceIntent;
	private Intent GlobalUpdateServiceIntent;
	private PendingIntent GlobalUpdatePendingIntent;
	private PendingIntent BlePendingIntent;
	
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

		statusPrefs = getSharedPreferences(StatusPrefs.PREFS_STATUS,
				Context.MODE_PRIVATE);

		setTimers();

	}

    public void setTimers(){

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

        setTimers();

		statusPrefs = getSharedPreferences(StatusPrefs.PREFS_STATUS,
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

        if (id == R.id.personal_overview_to_beacons) {
            Intent intent = new Intent(this, BeaconOverviewActivity.class);
            startActivity(intent);
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
					Editor sensorEditor = sAuthPrefs.edit();
					sensorEditor.putLong(Sensors.SENSOR_LIST_COMPLETE_TIME, 0);
					sensorEditor.putString(Sensors.SENSOR_LIST_COMPLETE, null);
					sensorEditor.putString(Sensors.BEACON_SENSOR, null);
					sensorEditor.commit();

					SharedPreferences sStatusPrefs = getSharedPreferences(
							StatusPrefs.PREFS_STATUS, Context.MODE_PRIVATE);
					Editor statusEditor = sStatusPrefs.edit();
					statusEditor.putBoolean(StatusPrefs.STATUS_IN_OFFICE, false);
					statusEditor.putLong(StatusPrefs.STATUS_TOTAL_TIME, 0);
					statusEditor.putLong(StatusPrefs.STATUS_TIME_TODAY, 0);
					statusEditor.putLong(StatusPrefs.STATUS_TIME_WEEK, 0);
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

		statusPrefs = getSharedPreferences(StatusPrefs.PREFS_STATUS,
				Context.MODE_PRIVATE);

		todayHours = (TextView) findViewById(R.id.personal_overview_today_hour);
		todayMinutes = (TextView) findViewById(R.id.personal_overview_today_minute);
		todaySeconds = (TextView) findViewById(R.id.personal_overview_today_seconds);

		thisWeekDays = (TextView) findViewById(R.id.personal_overview_this_week_day);
		thisWeekHours = (TextView) findViewById(R.id.personal_overview_this_week_hour);
		thisWeekMinutes = (TextView) findViewById(R.id.personal_overview_this_week_minute);
		thisWeekSeconds = (TextView) findViewById(R.id.personal_overview_this_week_second);

		thisLifeDays = (TextView) findViewById(R.id.personal_overview_this_life_day);
		thisLifeHours = (TextView) findViewById(R.id.personal_overview_this_life_hour);
		thisLifeMinutes = (TextView) findViewById(R.id.personal_overview_this_life_minute);
		thisLifeSeconds = (TextView) findViewById(R.id.personal_overview_this_life_second);
		
		status = (TextView) findViewById(R.id.personal_overview_status);

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

	public void toGroup(View view){
		
		Intent intent = new Intent(this, GroupOverviewActivity.class);
		startActivity(intent);
	}
	private Handler timerHandler = new Handler();

	private Runnable updateTimerThread = new Runnable() {

		long currentTimeInSeconds;
		long timeDifferenceSeconds;
		long displayTime;

		public void run() {

			if (statusPrefs.getBoolean(StatusPrefs.STATUS_IN_OFFICE, false)) {

				status.setText("Status: in the sense office");
				currentTimeInSeconds = System.currentTimeMillis() / 1000;
				timeDifferenceSeconds = currentTimeInSeconds
						- statusPrefs.getLong(StatusPrefs.STATUS_TIMESTAMP, 0);

				displayTime = statusPrefs.getLong(StatusPrefs.STATUS_TOTAL_TIME, 0)
						+ timeDifferenceSeconds;

                Clock clock = new Clock(displayTime);

				thisLifeDays.setText(clock.getDays());
				thisLifeHours.setText(clock.getHours());
				thisLifeMinutes.setText(clock.getMinutes());
				thisLifeSeconds.setText(clock.getSeconds());
						
				displayTime = statusPrefs.getLong(StatusPrefs.STATUS_TIME_WEEK, 0)
						+ timeDifferenceSeconds;

                clock = new Clock(displayTime);

                thisWeekDays.setText(clock.getDays());
                thisWeekHours.setText(clock.getHours());
                thisWeekMinutes.setText(clock.getMinutes());
                thisWeekSeconds.setText(clock.getSeconds());
				
				displayTime = statusPrefs.getLong(StatusPrefs.STATUS_TIME_TODAY, 0)
						+ timeDifferenceSeconds;

                clock = new Clock(displayTime);
				
				todayHours.setText(clock.getHours());
				todayMinutes.setText(clock.getMinutes());
				todaySeconds.setText(clock.getSeconds());

			} else {
				status.setText("Status: not in the sense office");

				displayTime = statusPrefs.getLong(StatusPrefs.STATUS_TOTAL_TIME, 0);
                Clock clock = new Clock(displayTime);

                thisLifeDays.setText(clock.getDays());
                thisLifeHours.setText(clock.getHours());
                thisLifeMinutes.setText(clock.getMinutes());
                thisLifeSeconds.setText(clock.getSeconds());
				
				displayTime = statusPrefs.getLong(StatusPrefs.STATUS_TIME_WEEK, 0);
                clock = new Clock(displayTime);

                thisWeekDays.setText(clock.getDays());
                thisWeekHours.setText(clock.getHours());
                thisWeekMinutes.setText(clock.getMinutes());
                thisWeekSeconds.setText(clock.getSeconds());

				displayTime = statusPrefs.getLong(StatusPrefs.STATUS_TIME_TODAY, 0);
                clock = new Clock(displayTime);

                todayHours.setText(clock.getHours());
                todayMinutes.setText(clock.getMinutes());
                todaySeconds.setText(clock.getSeconds());
			}
			timerHandler.postDelayed(this, 1000);
		}
	};
}
