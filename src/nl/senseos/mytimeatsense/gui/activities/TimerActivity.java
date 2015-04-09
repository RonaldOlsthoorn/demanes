package nl.senseos.mytimeatsense.gui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import nl.senseos.mytimeatsense.R;
import nl.senseos.mytimeatsense.util.Clock;
import nl.senseos.mytimeatsense.util.Constants;

public class TimerActivity extends Activity {

    private String timerType;
    private SharedPreferences statusPrefs;
    private Handler timerHandler;

    private TextView mSeconds;
    private TextView mMinutes;
    private TextView mHours;
    private TextView mDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        timerType = getIntent().getStringExtra(PersonalOverviewActivity.TIMER_TYPE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume(); // Always call the superclass method first

        statusPrefs = getSharedPreferences(Constants.Status.PREFS_STATUS,
                Context.MODE_PRIVATE);

        mSeconds = (TextView) findViewById(R.id.timer_second);
        mMinutes = (TextView) findViewById(R.id.timer_minute);
        mHours = (TextView) findViewById(R.id.timer_hour);
        mDays = (TextView) findViewById(R.id.timer_day);

        timerHandler = new Handler();

        if(timerType.equals(PersonalOverviewActivity.TIMER_BIKE)){
            timerHandler.postDelayed(updateBikeTimerThread, 0);
            ((TextView) findViewById(R.id.timer_header)).setText("Time spent on your SmartBike today");
        }
        if(timerType.equals(PersonalOverviewActivity.TIMER_DESK)){
            timerHandler.postDelayed(updateOfficeTimerThread, 0);
            ((TextView) findViewById(R.id.timer_header)).setText("Time spent in your SmartOffice today");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
        timerHandler.removeCallbacks(updateBikeTimerThread);
        timerHandler.removeCallbacks(updateOfficeTimerThread);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
        timerHandler.removeCallbacks(updateBikeTimerThread);
        timerHandler.removeCallbacks(updateOfficeTimerThread);
    }


    private Runnable updateBikeTimerThread = new Runnable() {

        Clock clock = new Clock();
        int key;
        long displayTime;

        public void run() {

            key = statusPrefs.getInt(Constants.Status.STATUS_DEVICE_KEY,0);

            if(key==5){
                displayTime = statusPrefs.getLong(Constants.Status.STATUS_TIME_BIKE,0)
                        + System.currentTimeMillis()/1000 - statusPrefs.getLong(Constants.Status.STATUS_TIMESTAMP,0);
            }else{
                displayTime = statusPrefs.getLong(Constants.Status.STATUS_TIME_BIKE,0);
            }

            clock.setTime(displayTime);

            mSeconds.setText(clock.getSeconds());
            mMinutes.setText(clock.getMinutes());
            mHours.setText(clock.getHours());
            mDays.setText(clock.getDays());


        timerHandler.postDelayed(this, 1000);
        }
    };

    private Runnable updateOfficeTimerThread = new Runnable() {

        Clock clock = new Clock();
        int key;
        long displayTime;

        public void run() {

            key = statusPrefs.getInt(Constants.Status.STATUS_DEVICE_KEY,0);

            if(key>0 && key<5){
                displayTime = statusPrefs.getLong(Constants.Status.STATUS_TIME_OFFICE,0)
                        + System.currentTimeMillis()/1000 - statusPrefs.getLong(Constants.Status.STATUS_TIMESTAMP,0);
            }else{
                displayTime = statusPrefs.getLong(Constants.Status.STATUS_TIME_OFFICE,0);
            }

            clock.setTime(displayTime);

            mSeconds.setText(clock.getSeconds());
            mMinutes.setText(clock.getMinutes());
            mHours.setText(clock.getHours());
            mDays.setText(clock.getDays());

            timerHandler.postDelayed(this, 1000);
        }
    };
}
