package nl.senseos.mytimeatsense.sync;

import java.util.GregorianCalendar;

import nl.senseos.mytimeatsense.bluetooth.BluetoothLeScanService;
import nl.senseos.mytimeatsense.bluetooth.iBeacon;
import nl.senseos.mytimeatsense.gui.activities.PersonalOverviewActivity;
import nl.senseos.mytimeatsense.storage.DBHelper;
import nl.senseos.mytimeatsense.util.Constants.Status;

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
	private iBeacon beacon;


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

        if(scanResult){
            beacon = intent.getParcelableExtra(BluetoothLeScanService.SCAN_RESULT_BEACON);
        }


		Log.d(TAG, "detected: " + scanResult + " ts: " + scanResultTS);

        DB = DBHelper.getDBHelper(this);
        ContentValues v = new ContentValues();

        if(!scanResult){
            // insert data point in database
            v.put(DBHelper.DetectionTable.COLUMN_TIMESTAMP, scanResultTS);
            v.put(DBHelper.DetectionTable.COLUMN_DETECTION_RESULT, scanResult);
            v.put(DBHelper.DetectionTable.COLUMN_DEVICE, Status.STATE_NOT_DETECTED);
            v.put(DBHelper.DetectionTable.COLUMN_DEVICE_OCCUPIED, false);


        }else{
            v.put(DBHelper.DetectionTable.COLUMN_TIMESTAMP, scanResultTS);
            v.put(DBHelper.DetectionTable.COLUMN_DETECTION_RESULT, scanResult);
            v.put(DBHelper.DetectionTable.COLUMN_DEVICE, beacon.getSmartDevice().getKey());
            v.put(DBHelper.DetectionTable.COLUMN_DEVICE_OCCUPIED, beacon.getSmartDevice().isOccupied());
        }

		DB.insertOrIgnore(DBHelper.DetectionTable.TABLE_NAME, v);

		// update current state in shared preferences
		statusPrefs = getSharedPreferences(
				Status.PREFS_STATUS, Context.MODE_PRIVATE);
		Editor prefsEditor = statusPrefs.edit();

        prefsEditor.putBoolean(Status.STATUS_DETECTED, scanResult);
        prefsEditor.putInt(Status.STATUS_DEVICE_KEY, beacon==null? 0: beacon.getSmartDevice().getKey());
        prefsEditor.putLong(Status.STATUS_TIMESTAMP, scanResultTS);
        prefsEditor.putLong(Status.STATUS_TIME_OFFICE, getTimeOffice(
                    beacon==null? 0: beacon.getSmartDevice().getKey(), statusPrefs.getLong(Status.STATUS_TIMESTAMP, 0)
            ));
        prefsEditor.putLong(Status.STATUS_TIME_BIKE, getTimeBike(
                    beacon==null? 0: beacon.getSmartDevice().getKey(), statusPrefs.getLong(Status.STATUS_TIMESTAMP, 0)
            ));

        prefsEditor.commit();

    }

	public long getTimeOffice(int deviceKey, long previousScanResultTs) {

        boolean previousScanResult = (deviceKey>0 && deviceKey<5);
		
		if(scanResultTS-previousScanResultTs>TIME_OUT_LIMIT ||(!previousScanResult && !scanResult)){
			return statusPrefs.getLong(Status.STATUS_TIME_OFFICE, 0);
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
				long delta = (1/2)*(scanResultTS-statusPrefs.getLong(Status.STATUS_TIMESTAMP, 0));
				return statusPrefs.getLong(Status.STATUS_TIME_OFFICE, 0)+delta;
			}		
	
			long delta =(scanResultTS-statusPrefs.getLong(Status.STATUS_TIMESTAMP, 0));
			return statusPrefs.getLong(Status.STATUS_TIME_OFFICE, 0)+delta;
		}
	}

    public long getTimeBike(int deviceKey, long previousScanResultTs) {

        boolean previousScanResult = deviceKey==5;

        if(scanResultTS-previousScanResultTs>TIME_OUT_LIMIT ||(!previousScanResult && !scanResult)){
            return statusPrefs.getLong(Status.STATUS_TIME_BIKE, 0);
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
                long delta = (1/2)*(scanResultTS-statusPrefs.getLong(Status.STATUS_TIMESTAMP, 0));
                return statusPrefs.getLong(Status.STATUS_TIME_OFFICE, 0)+delta;
            }

            long delta =(scanResultTS-statusPrefs.getLong(Status.STATUS_TIMESTAMP, 0));
            return statusPrefs.getLong(Status.STATUS_TIME_OFFICE, 0)+delta;
        }
    }
}
