package nl.senseos.mytimeatsense.gui.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import nl.senseos.mytimeatsense.R;
import nl.senseos.mytimeatsense.storage.DBHelper;

public class BeaconOverviewActivity extends Activity {

    private static final int REQUEST_NEW_BEACON = 10;
    private DBHelper db;
    private SimpleCursorAdapter beaconAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_overview);

        db = DBHelper.getDBHelper(this);
        Cursor c = db.getAllBeacons();
        beaconAdapter = new SimpleCursorAdapter(this, R.layout.listitem_device,c,
                new String[]{DBHelper.BeaconTable.COLUMN_MAJOR, DBHelper.BeaconTable.COLUMN_MINOR},
                new int[]{R.id.listitem_device_id, R.id.listitem_device_name},0);

        ((ListView) findViewById(R.id.beaconList)).setAdapter(beaconAdapter);

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

    public void addBeacon(View view){
        Intent intent = new Intent(this, AddBeaconActivity.class);
        startActivityForResult(intent, REQUEST_NEW_BEACON);
    }
}