package nl.senseos.mytimeatsense.sync;

import java.io.IOException;
import java.util.GregorianCalendar;

import nl.senseos.mytimeatsense.commonsense.CommonSenseAdapter;
import nl.senseos.mytimeatsense.commonsense.MsgHandler;
import nl.senseos.mytimeatsense.util.Constants.Auth;
import nl.senseos.mytimeatsense.storage.DBHelper;


import org.json.JSONException;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

public class StatusUpdateService extends IntentService {

	private static final String TAG = StatusUpdateService.class.getSimpleName();
	private CommonSenseAdapter cs;
	private SharedPreferences authPrefs;
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

			uploadLocalStatus();

			// logout
			try {
				cs.logout();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

    private void uploadLocalStatus() {

        try {
            // update and upload to CS
            cs.sendStatus();

            DB.deleteAllRows(DBHelper.DetectionTable.TABLE_NAME);
        }catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
}
