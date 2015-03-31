package nl.senseos.mytimeatsense.gui.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import nl.senseos.mytimeatsense.R;
import nl.senseos.mytimeatsense.storage.DBHelper;

public class BeaconOverviewActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final int REQUEST_NEW_BEACON = 10;
    private DBHelper db;
    private SimpleCursorAdapter beaconAdapter;
    private static final String TAG = BeaconOverviewActivity.class.getSimpleName();
    private Cursor cursor;

    public static final String BEACON_ID = "beacon_id";
    public static final String BEACON_ADDRESS = "beacon_address";
    public static final String BEACON_NAME = "beacon_name";
    public static final String BEACON_UUID = "beacon_uuid";
    public static final String BEACON_MAJOR = "beacon_major";
    public static final String BEACON_MINOR = "beacon_minor";
    public static final String BEACON_TX = "beacon_tx";
    public static final String BEACON_REMOTE = "beacon_remote";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_overview);

        db = DBHelper.getDBHelper(this);
        cursor = db.getAllVisibleBeacons();
        beaconAdapter = new SimpleCursorAdapter(this, R.layout.listitem_device,cursor,
                new String[]{DBHelper.BeaconTable.COLUMN_NAME_NAME},
                new int[]{R.id.listitem_device_name},0);

        ((ListView) findViewById(R.id.beaconList)).setAdapter(beaconAdapter);
        ((ListView) findViewById(R.id.beaconList)).setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_beacon_overview, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.

        if (requestCode == REQUEST_NEW_BEACON
                && resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        if (requestCode == REQUEST_NEW_BEACON
                && resultCode == Activity.RESULT_OK) {
            beaconAdapter.swapCursor(db.getAllVisibleBeacons());
            beaconAdapter.notifyDataSetChanged();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        cursor = db.getAllVisibleBeacons();
        beaconAdapter.swapCursor(cursor);
        beaconAdapter.notifyDataSetChanged();
    }

    public void addBeacon(View view){
        Intent intent = new Intent(this, AddBeaconActivity.class);
        startActivityForResult(intent, REQUEST_NEW_BEACON);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        cursor.moveToPosition(position);

        Intent intent = new Intent(this, BeaconActivity.class);
        intent.putExtra(BEACON_ID, cursor.getInt(0));
        intent.putExtra(BEACON_ADDRESS, cursor.getString(1));
        intent.putExtra(BEACON_NAME, cursor.getString(1));
        intent.putExtra(BEACON_UUID, cursor.getString(2));
        intent.putExtra(BEACON_MAJOR, cursor.getInt(3));
        intent.putExtra(BEACON_MINOR, cursor.getInt(4));
        intent.putExtra(BEACON_TX, cursor.getInt(5));
        intent.putExtra(BEACON_REMOTE, cursor.getInt(6));

        startActivity(intent);
    }
}