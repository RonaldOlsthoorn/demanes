package nl.senseos.mytimeatsense.sync;

import java.util.GregorianCalendar;

import nl.senseos.mytimeatsense.bluetooth.BluetoothLeScanService;
import nl.senseos.mytimeatsense.gui.activities.PersonalOverviewActivity;
import nl.senseos.mytimeatsense.storage.DBHelper;
import nl.senseos.mytimeatsense.util.Constants.StatusPrefs;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class LocalUpdateService extends IntentService {

	private final static String TAG = IntentService.class.getSimpleName();
	public static final long TIME_OUT_LIMIT = (PersonalOverviewActivity.REPEAT_INTEVAL_MINS_BLE*60*2);
	private DBHelper DB;
	private boolean scanResult;
	private long scanResultTS;
	private long scanResultId;
	SharedPreferences statusPrefs;

	public LocalUpdateService() {
		super(TAG);
	}

	/**
	 * Update the shared preferences, database with result ble
	 * scan.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {

		// retreive latest scan result
		scanResult = intent.getBooleanExtra(
				BluetoothLeScanService.SCAN_RESULT, false);
		scanResultTS = intent.getLongExtra(
				BluetoothLeScanService.SCAN_RESULT_TIMESTAMP, 0);
		scanResultId = intent.getIntExtra(
				BluetoothLeScanService.SCAN_RESULT_ID, -1);

		Log.d(TAG, "detected: " + scanResult + " ts: " + scanResultTS);

		// insert data point in database
		DB = DBHelper.getDBHelper(this);
		ContentValues v = new ContentValues();
		v.put(DBHelper.DetectionTable.COLUMN_TIMESTAMP, scanResultTS);
		v.put(DBHelper.DetectionTable.COLUMN_DETECTION_RESULT, scanResult);
		v.put(DBHelper.DetectionTable.COLUMN_BEACON, scanResultId);
		
		DB.insertOrIgnore(DBHelper.DetectionTable.TABLE_NAME, v);

		// update current state in shared preferences
		statusPrefs = getSharedPreferences(
				StatusPrefs.PREFS_STATUS, Context.MODE_PRIVATE);
		Editor prefsEditor = statusPrefs.edit();
		
		prefsEditor.putBoolean(StatusPrefs.STATUS_IN_OFFICE, scanResult);
		prefsEditor.putLong(StatusPrefs.STATUS_TIMESTAMP, scanResultTS);
		prefsEditor.putLong(StatusPrefs.STATUS_TOTAL_TIME, getTotalTimeUpdate());
		prefsEditor.putLong(StatusPrefs.STATUS_TIME_WEEK, getThisWeekTimeUpdate());
		prefsEditor.putLong(StatusPrefs.STATUS_TIME_TODAY, getTodayTimeUpdate());
		prefsEditor.commit();
	}

	public long getTotalTimeUpdate() {
		
		long previousScanResultTs = statusPrefs.getLong(StatusPrefs.STATUS_TIMESTAMP, 0);
		boolean previousScanResult = statusPrefs.getBoolean(StatusPrefs.STATUS_IN_OFFICE, false);
		
		if(scanResultTS-previousScanResultTs>TIME_OUT_LIMIT ||(!previousScanResult && !scanResult)){
			return statusPrefs.getLong(StatusPrefs.STATUS_TOTAL_TIME, 0);
		}
		if((!previousScanResult && scanResult) || (previousScanResult && !scanResult)){
			
			long delta = (1/2)*(scanResultTS-statusPrefs.getLong(StatusPrefs.STATUS_TIMESTAMP, 0));
			return statusPrefs.getLong(StatusPrefs.STATUS_TOTAL_TIME, 0)+delta;
		}		
		long delta =(scanResultTS-statusPrefs.getLong(StatusPrefs.STATUS_TIMESTAMP, 0));
		return statusPrefs.getLong(StatusPrefs.STATUS_TOTAL_TIME, 0)+delta;
	}

	public long getTodayTimeUpdate() {
		
		long previousScanResultTs = statusPrefs.getLong(StatusPrefs.STATUS_TIMESTAMP, 0);
		boolean previousScanResult = statusPrefs.getBoolean(StatusPrefs.STATUS_IN_OFFICE, false);
		
		if(scanResultTS-previousScanResultTs>TIME_OUT_LIMIT ||(!previousScanResult && !scanResult)){
			return statusPrefs.getLong(StatusPrefs.STATUS_TIME_TODAY, 0);
		}
		
		GregorianCalendar calMidnight = new GregorianCalendar();
		calMidnight.set(GregorianCalendar.HOUR_OF_DAY, 0);
		calMidnight.set(GregorianCalendar.MINUTE, 0);
		calMidnight.set(GregorianCalendar.SECOND, 0);
		
		if(previousScanResultTs < calMidnight.getTimeInMillis()/1000 ){
			
			if(!scanResult){
				return 0;
			}
			if(!previousScanResult){
				long delta =(1/2)*(scanResultTS-(calMidnight.getTimeInMillis()/1000));
				return delta;
			}
			long delta =(scanResultTS-(calMidnight.getTimeInMillis()/1000));
			return delta;
		}
		else{
			if((!previousScanResult && scanResult) || (previousScanResult && !scanResult)){
				long delta = (1/2)*(scanResultTS-statusPrefs.getLong(StatusPrefs.STATUS_TIMESTAMP, 0));
				return statusPrefs.getLong(StatusPrefs.STATUS_TIME_TODAY, 0)+delta;
			}		
	
			long delta =(scanResultTS-statusPrefs.getLong(StatusPrefs.STATUS_TIMESTAMP, 0));		
			return statusPrefs.getLong(StatusPrefs.STATUS_TIME_TODAY, 0)+delta;			
		}
	}

	public long getThisWeekTimeUpdate() {

		long previousScanResultTs = statusPrefs.getLong(StatusPrefs.STATUS_TIMESTAMP, 0);
		boolean previousScanResult = statusPrefs.getBoolean(StatusPrefs.STATUS_IN_OFFICE, false);
		
		if(scanResultTS-previousScanResultTs>TIME_OUT_LIMIT ||(!previousScanResult && !scanResult)){
			return statusPrefs.getLong(StatusPrefs.STATUS_TIME_WEEK, 0);
		}
		
		GregorianCalendar calMondayMidnight = new GregorianCalendar();
		calMondayMidnight.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.MONDAY);
		calMondayMidnight.set(GregorianCalendar.HOUR_OF_DAY, 0);
		calMondayMidnight.set(GregorianCalendar.MINUTE, 0);
		calMondayMidnight.set(GregorianCalendar.SECOND, 0);
		
		if(previousScanResultTs < calMondayMidnight.getTimeInMillis()/1000 ){
			
			if(!scanResult){
				return 0;
			}
			if(!previousScanResult){
				long delta =(1/2)*(scanResultTS-(calMondayMidnight.getTimeInMillis()/1000));
				return delta;
			}
			long delta =(scanResultTS-(calMondayMidnight.getTimeInMillis()/1000));
			return delta;
		}
		else{
			if((!previousScanResult && scanResult) || (previousScanResult && !scanResult)){
				long delta = (1/2)*(scanResultTS-statusPrefs.getLong(StatusPrefs.STATUS_TIMESTAMP, 0));
				return statusPrefs.getLong(StatusPrefs.STATUS_TIME_WEEK, 0)+delta;
			}		
			long delta =(scanResultTS-statusPrefs.getLong(StatusPrefs.STATUS_TIMESTAMP, 0));
			return statusPrefs.getLong(StatusPrefs.STATUS_TIME_WEEK, 0)+delta;			
		}
	}
}
