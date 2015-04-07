package nl.senseos.mytimeatsense.gui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;

import nl.senseos.mytimeatsense.R;
import nl.senseos.mytimeatsense.util.Constants;


public class LightSettingsActivity extends Activity {

    SharedPreferences statusPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_settings);

        statusPrefs=getSharedPreferences(Constants.Status.PREFS_STATUS,
                Context.MODE_PRIVATE);

        SeekBar lightLevel = (SeekBar) findViewById(R.id.seekBar_level);
        lightLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor statusEditor = statusPrefs.edit();
                statusEditor.putInt(Constants.Status.STATUS_LIGHT_LEVEL, seekBar.getProgress());
            }
        });

        SeekBar lightFocus = (SeekBar) findViewById(R.id.seekBar_focus);
        lightFocus.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor statusEditor = statusPrefs.edit();
                statusEditor.putInt(Constants.Status.STATUS_LIGHT_FOCUS, seekBar.getProgress());
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_light_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
