package nl.senseos.mytimeatsense.gui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import nl.senseos.mytimeatsense.R;
import nl.senseos.mytimeatsense.bluetooth.iBeacon;
import nl.senseos.mytimeatsense.storage.DBHelper;

public class BeaconActivity extends Activity {

    private iBeacon beacon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);

        Intent intent = getIntent();

        beacon = new iBeacon(
                intent.getStringExtra(BeaconOverviewActivity.BEACON_ADDRESS),
                intent.getStringExtra(BeaconOverviewActivity.BEACON_NAME),
                intent.getStringExtra(BeaconOverviewActivity.BEACON_UUID),
                intent.getIntExtra(BeaconOverviewActivity.BEACON_MAJOR, -1),
                intent.getIntExtra(BeaconOverviewActivity.BEACON_MINOR, -1),
                intent.getIntExtra(BeaconOverviewActivity.BEACON_TX, -1)
                );

        beacon.setLocalId(intent.getIntExtra(BeaconOverviewActivity.BEACON_ID, -1));

        ((TextView) findViewById(R.id.beacon_name)).setText(beacon.getName());
        ((TextView) findViewById(R.id.beacon_adress)).setText(beacon.getAdress());
        ((TextView) findViewById(R.id.beacon_uuid)).setText(beacon.getUUID());
        ((TextView) findViewById(R.id.beacon_major)).setText(Integer.toString(beacon.getMajor()));
        ((TextView) findViewById(R.id.beacon_minor)).setText(Integer.toString(beacon.getMinor()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_beacon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    public void forgetBeacon(View view){
        DBHelper db = DBHelper.getDBHelper(this);
        boolean res = beacon.deleteDB(db);

        Toast.makeText(this, "Beacon deleted", Toast.LENGTH_LONG).show();
        finish();
    }
}