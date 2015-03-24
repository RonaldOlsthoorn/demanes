package nl.senseos.mytimeatsense.sync;

import java.io.IOException;
import java.util.GregorianCalendar;

import nl.senseos.mytimeatsense.commonsense.CommonSenseAdapter;
import nl.senseos.mytimeatsense.commonsense.MsgHandler;
import nl.senseos.mytimeatsense.util.Constants.Auth;
import nl.senseos.mytimeatsense.storage.DBHelper;
import nl.senseos.mytimeatsense.util.Constants.StatusPrefs;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;

public class StatusUpdateService extends IntentService {

	private static final String TAG = StatusUpdateService.class.getSimpleName();
	private CommonSenseAdapter cs;
	private SharedPreferences authPrefs;
	private SharedPreferences statusPrefs;
	private DBHelper DB;

	private GregorianCalendar calMidnight;
	private GregorianCalendar calMondayMidnight;

	public StatusUpdateService() {
		super(TAG);
	}

	protected void onHandleIntent(Intent intent) {

		Log.v(TAG, "global update status");
		
		DB = DBHelper.getDBHelper(this);
		
		authPrefs = getSharedPreferences(Auth.PREFS_CREDS, Context.MODE_PRIVATE);
		statusPrefs = getSharedPreferences(StatusPrefs.PREFS_STATUS,
				Context.MODE_PRIVATE);
		
		cs = new CommonSenseAdapter(this);
		
		Handler updateHandler = new Handler(MsgHandler.getInstance().getLooper());
		updateHandler.post(new GlobalUpdateRunnable());
	}
	
	private class GlobalUpdateRunnable implements Runnable{

		@Override
		public void run() {
			String mEmail = authPrefs.getString(Auth.PREFS_CREDS_UNAME, null);
			String mPassword = authPrefs.getString(Auth.PREFS_CREDS_PASSWORD, null);

			if (mEmail == null || mPassword == null) {
				Log.v(TAG, "no creds, return");
				return;
			}

			// login to commonsense
			Log.v(TAG, "try to login...");
			int loginSuccess = cs.login();
			Log.v(TAG, "login success: " + loginSuccess);

			// login returns 0 if successful.
			if (loginSuccess != 0) {
				return;
			}
			// check if sensor is present
			boolean sensorPresent = false;
			try {
				sensorPresent = cs.hasBeaconSensor();
				// if not present, make one!
				if (!sensorPresent) {
					sensorPresent = cs.registerBeaconSensor();
				}
			} catch (IOException | JSONException e1) {
				e1.printStackTrace();
			}
			if (sensorPresent) {
				Log.v(TAG, "full CS sync");
				fullSyncStatus();
			}

			// logout
			try {
				cs.logout();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void fullSyncStatus() {

        uploadLocalStatus();

		try {
			// fetch latest status and update the status in SharedPreferences
            JSONObject response = cs.getTotalTime();
			if (response == null) {
				return;
			}
			JSONObject valueCurrent = response;

			calMidnight = new GregorianCalendar();
			calMidnight.set(GregorianCalendar.HOUR_OF_DAY, 0);
			calMidnight.set(GregorianCalendar.MINUTE, 0);
			calMidnight.set(GregorianCalendar.SECOND, 0);

			response = cs
					.getStatusBefore(calMidnight.getTimeInMillis() / 1000);

			JSONObject statusBeforeMidnight = response;

			response = cs
					.getStatusAfter(calMidnight.getTimeInMillis() / 1000);

			JSONObject statusAfterMidnight = response;

			calMondayMidnight = new GregorianCalendar();
			calMondayMidnight.set(GregorianCalendar.DAY_OF_WEEK,
					GregorianCalendar.MONDAY);
			calMondayMidnight.set(GregorianCalendar.HOUR_OF_DAY, 0);
			calMondayMidnight.set(GregorianCalendar.MINUTE, 0);
			calMondayMidnight.set(GregorianCalendar.SECOND, 0);
			
			response = cs
					.getStatusBefore(calMondayMidnight.getTimeInMillis() / 1000);
			JSONObject statusBeforeMondayMidnight = response;

			response = cs
					.getStatusAfter(calMondayMidnight.getTimeInMillis() / 1000);
			JSONObject statusAfterMondayMidnight = response;

			updateStatusPrefs(valueCurrent, statusBeforeMidnight,
					statusAfterMidnight, statusBeforeMondayMidnight,
					statusAfterMondayMidnight);

		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}

    private void uploadLocalStatus() {

        if(DB.getCompleteLog().getCount()==0){
            return;
        }

        try {
            // first get the latest update from CS
            JSONObject response = cs.getTotalTime();

            JSONObject dataPackage;
            // no data points present on CS, upload local value as is
            if (response == null) {
                dataPackage = dbToJSON(false, 0, 0);
            } else {
                // obtain latest update from response
                long lastUpdateTs = response.getLong("date");
                JSONObject value = new JSONObject(response.getString("value"));
                boolean lastUpdateStatus = value.getBoolean("status");
                long lastUpdateTotalTime = value.getLong("total_time");

                dataPackage = dbToJSON(lastUpdateStatus, lastUpdateTs,
                        lastUpdateTotalTime);
            }
            // update and upload to CS
            cs.sendBeaconData(dataPackage);
            DB.deleteAllRows(DBHelper.DetectionTable.TABLE_NAME);
        }catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculates the new status: total time, time spent this week and time spent today and
     * saves the new status in the shared preferences.
     *
     * @param valueCurrent JSONObject containing the latest status on CS.
     * @param valueBeforeMidnight JSONObject containing the latest status on CS before midnight.
     * @param valueAfterMidnight JSONObject containing the first status on CS after midnight
     * @param valueBeforeMondayMidnight JSONObject containing the latest status on CS before the
     * the beginning of this week
     * @param valueAfterMondayMidnight
     */
    private void updateStatusPrefs(JSONObject valueCurrent,
			JSONObject valueBeforeMidnight, JSONObject valueAfterMidnight,
			JSONObject valueBeforeMondayMidnight,
			JSONObject valueAfterMondayMidnight) {

		try {
			Editor statusEditor = statusPrefs.edit();

			long totalTime = new JSONObject(valueCurrent.getString("value"))
					.getLong("total_time");
			boolean statusCurrent = new JSONObject(
					valueCurrent.getString("value")).getBoolean("status");
			long timeStampCurrent = valueCurrent.getLong("date");

			statusEditor.putLong(StatusPrefs.STATUS_TOTAL_TIME, totalTime);
			statusEditor.putBoolean(StatusPrefs.STATUS_IN_OFFICE, statusCurrent);
			statusEditor.putLong(StatusPrefs.STATUS_TIMESTAMP, timeStampCurrent);

			long todayMidnight;

            // No measurement before midnight. Take the first after midnight
			if (valueBeforeMidnight == null) {
				todayMidnight = new JSONObject(valueAfterMidnight
						.getString("value")).getLong("total_time");
			}
            // No measurement after midnight. Take the first before midnight
            else if (valueAfterMidnight == null) {
				todayMidnight = new JSONObject(valueBeforeMidnight
						.getString("value")).getLong("total_time");
			}
            // Both before and after midnight, measurement is available.
            else {

				long beforeMidnight = new JSONObject(valueBeforeMidnight
						.getString("value")).getLong("total_time");
				boolean beforeMidnightStatus = new JSONObject(
						valueBeforeMidnight.getString("value"))
						.getBoolean("status");
				long beforeMidnightTS = valueBeforeMidnight.getLong("date");
				long afterMidnight = new JSONObject(valueAfterMidnight
						.getString("value")).getLong("total_time");
				boolean afterMidnightStatus = new JSONObject(
						valueBeforeMidnight.getString("value"))
						.getBoolean("status");
				long afterMidnightTS = valueAfterMidnight.getLong("date");


				if ((!beforeMidnightStatus && !afterMidnightStatus)
						|| (afterMidnightTS - beforeMidnightTS > LocalUpdateService.TIME_OUT_LIMIT)) {
					todayMidnight = beforeMidnight;
				} else if (beforeMidnightStatus && !afterMidnightStatus  ||
                        !beforeMidnightStatus && afterMidnightStatus) {
					todayMidnight = afterMidnight;
				} else {
					todayMidnight = afterMidnight
							- (afterMidnightTS - (calMidnight
									.getTimeInMillis() / 1000));
				}
			}
			statusEditor.putLong(StatusPrefs.STATUS_TIME_TODAY, totalTime
					- todayMidnight);

			long mondayMidnight;

			if (valueBeforeMondayMidnight == null) {
				
				mondayMidnight = new JSONObject(valueAfterMondayMidnight
						.getString("value")).getLong("total_time");
			} else if (valueAfterMondayMidnight == null) {

				mondayMidnight = new JSONObject(valueBeforeMondayMidnight
						.getString("value")).getLong("total_time");
			} else {

				long beforeMidnight = new JSONObject(valueBeforeMondayMidnight
						.getString("value")).getLong("total_time");
				boolean beforeMidnightStatus = new JSONObject(
						valueBeforeMondayMidnight.getString("value"))
						.getBoolean("status");
				long beforeMidnightTS = valueBeforeMondayMidnight.getLong("date");
				long afterMidnight = new JSONObject(valueAfterMondayMidnight
						.getString("value")).getLong("total_time");
				long afterMidnightTS = valueAfterMondayMidnight.getLong("date");
				boolean afterMidnightStatus = new JSONObject(
						valueBeforeMondayMidnight.getString("value"))
						.getBoolean("status");

				if ((!beforeMidnightStatus && !afterMidnightStatus)
						|| (afterMidnightTS - beforeMidnightTS > LocalUpdateService.TIME_OUT_LIMIT)) {
					mondayMidnight = beforeMidnight;
				} else if (beforeMidnightStatus && !afterMidnightStatus) {
					mondayMidnight = afterMidnight;
				} else if (!beforeMidnightStatus && afterMidnightStatus) {
					mondayMidnight = afterMidnight;
				} else {
					mondayMidnight = afterMidnight
							- (afterMidnightTS - (calMondayMidnight
									.getTimeInMillis() / 1000));
				}
			}
			statusEditor.putLong(StatusPrefs.STATUS_TIME_WEEK, totalTime
					- mondayMidnight);

			statusEditor.commit();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public JSONObject dbToJSON(boolean initStatus, long lastUpdateTs,
			long lastUpdateTotal) throws JSONException {

		JSONObject res = new JSONObject();
		JSONArray dataArray = new JSONArray();
		JSONObject dataToken;

		long total = lastUpdateTotal;

		boolean follower = initStatus;
		long followerTs = lastUpdateTs;

		boolean leader;
		long leaderTs = 0;
		
		Cursor log = DB.getCompleteLog();

		// Time increments are calculated for each pair of consecutive scans: The leader
        // and Follower.
		while (log.moveToNext()) {

			dataToken = new JSONObject();

			leader = log.getInt(2) > 0;
			leaderTs = log.getLong(1);

            // if time span between two scans is too big, consider the intermediate time
            // as NOT spent at the office. Also when two consecutive scans detected absent
			if (leaderTs - followerTs > LocalUpdateService.TIME_OUT_LIMIT
					|| (!leader && !follower)) {
				Log.e(TAG,"time out "+leaderTs+","+followerTs);
				dataToken.put("value", "{\"total_time\":" + total + ","
						+ "\"status\":" + Boolean.toString(leader) + "}");
			}
            // if a sequence of present - absent is detected in consecutive scans
            // or reverse, approximately half of the time is spent at the office.
            else if ((!leader && follower) ||(leader && !follower) ) {
				long delta = (1 / 2) * (leaderTs - followerTs);
				total = total + delta;
				dataToken.put("value", "{\"total_time\":" + total + ","
						+ "\"status\":" + Boolean.toString(leader) + "}");
			} else {
				long delta = (leaderTs - followerTs);
				total = total + delta;
				dataToken.put("value", "{\"total_time\":" + total + ","
						+ "\"status\":" + Boolean.toString(leader) + "}");
			}
			dataToken.put("date", leaderTs);
			dataArray.put(dataToken);

			follower = leader;
			followerTs = leaderTs;
		}
		res.put("data", dataArray);
		return res;
	}
}
