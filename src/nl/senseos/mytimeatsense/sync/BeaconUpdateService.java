package nl.senseos.mytimeatsense.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

import nl.senseos.mytimeatsense.bluetooth.iBeacon;
import nl.senseos.mytimeatsense.commonsense.CommonSenseAdapter;
import nl.senseos.mytimeatsense.commonsense.MsgHandler;
import nl.senseos.mytimeatsense.storage.DBHelper;
import nl.senseos.mytimeatsense.util.Constants;

public class BeaconUpdateService extends IntentService {

    public static final String TAG = BeaconUpdateService.class.getSimpleName();
    private CommonSenseAdapter cs;
    private SharedPreferences authPrefs;
    private DBHelper DB;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public BeaconUpdateService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.v(TAG, "global beacon update");

        DB = DBHelper.getDBHelper(this);
        authPrefs = getSharedPreferences(Constants.Auth.PREFS_CREDS, Context.MODE_PRIVATE);
        cs = new CommonSenseAdapter(this);

        Handler updateHandler = new Handler(MsgHandler.getInstance().getLooper());
        updateHandler.post(new GlobalUpdateRunnable());
    }

    private class GlobalUpdateRunnable implements Runnable {

        @Override
        public void run() {
            String mEmail = authPrefs.getString(Constants.Auth.PREFS_CREDS_UNAME, null);
            String mPassword = authPrefs.getString(Constants.Auth.PREFS_CREDS_PASSWORD, null);

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

            // check if beacon label is present
            boolean labelPresent = false;
            try {
                labelPresent = cs.hasLabel(Constants.Labels.LABEL_NAME_OFFICE);

                Log.e(TAG, "label present initial: "+labelPresent);
                // if not present, make one!
                if (!labelPresent) {
                    labelPresent = cs.registerLabel(Constants.Labels.LABEL_NAME_OFFICE, "") != -1;
                    Log.e(TAG, "label present after creation: "+labelPresent);
                }
            } catch (IOException | JSONException e1) {
                e1.printStackTrace();
            }
            if (labelPresent) {
                Log.v(TAG, "full CS sync");
                fullSyncBeacons();
            }
            // logout
            try {
                cs.logout();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void fullSyncBeacons() {

        Cursor addedBeacons = DB.getUnsavedBeacons();

        Log.e(TAG,"added beacons count: "+addedBeacons.getCount() );

        while (addedBeacons.moveToNext()) {

            iBeacon beacon = new iBeacon(
                    addedBeacons.getInt(0),
                    addedBeacons.getInt(6),
                    addedBeacons.getString(1),
                    addedBeacons.getString(2),
                    addedBeacons.getInt(3),
                    addedBeacons.getInt(4),
                    addedBeacons.getInt(5)
            );

            try {
                int remoteId = cs.registerBeacon(beacon);

                Log.e(TAG, "remote id: "+ remoteId);

                beacon.setRemoteId(remoteId);
                beacon.updateDB(DB);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Cursor removedBeacons = DB.getDeletedBeacons();

        Log.e(TAG,"removed beacons count: "+removedBeacons.getCount() );

        while (removedBeacons.moveToNext()) {
            try {
                cs.removeBeacon(removedBeacons.getInt(6));
                DB.deleteOrIgnore(DBHelper.BeaconTable.TABLE_NAME, removedBeacons.getInt(0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //remove from here

        Cursor c2 = DB.getAllBeacons();

        while(c2.moveToNext()){
            Log.e(TAG, "beacon "
                            +c2.getInt(0)+" "
                            +c2.getString(1)+" "
                            +c2.getString(2)+" "
                            +c2.getString(3)+" "
                            +c2.getInt(4)+" "
                            +c2.getInt(5)+" "
                            +c2.getInt(6)+" "
                            +c2.getInt(7)
            );
        }
        // to here

    }
}