package nl.senseos.mytimeatsense.bluetooth;

import nl.senseos.mytimeatsense.storage.DBHelper;
import nl.senseos.mytimeatsense.sync.LocalUpdateService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 *  Service used to scan for Bluetooth Low Energy Beacons
 */
public class BluetoothLeScanService extends Service {

	public final static String SCAN_RESULT = "ble_scan_result";
	public final static String SCAN_RESULT_TIMESTAMP = "ble_scan_result_timestamp";
    public final static String SCAN_RESULT_ID = "ble_scan_result_id";
	public final static String SCAN_RESULT_MAJOR = "ble_scan_result_major";
	public final static String SCAN_RESULT_MINOR = "ble_scan_result_minor";
	private final static String TAG = BluetoothLeScanService.class
			.getSimpleName();
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	// Stops scanning after 1 second.
	public static final long SCAN_PERIOD = 1 * 1000l;
	private boolean beaconFound = false;
	private boolean mScanning;
	private iBeacon proximity;
    private DBHelper db;

    /**
     *  Broadcast intent to update locally. Result of the scan is sent along
     */
	private void broadcastLocalUpdate() {

		final Intent intent = new Intent(this, LocalUpdateService.class);
		intent.putExtra(SCAN_RESULT, beaconFound);
		intent.putExtra(SCAN_RESULT_TIMESTAMP,
				System.currentTimeMillis() / 1000); // (in full seconds!)
		if(beaconFound){
            intent.putExtra(SCAN_RESULT_ID, proximity.getLocalId());
			intent.putExtra(SCAN_RESULT_MAJOR, proximity.getMajor());
			intent.putExtra(SCAN_RESULT_MINOR, proximity.getMinor());			
		}	
		startService(intent);
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, startId, startId);

        // if initialization succeeds, scan for devices
        if(initialize()){
            db = DBHelper.getDBHelper(this);
            scanLeDevice();
        }
        return START_STICKY;
    }

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 *
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}

	// Scan for bluetooth devices and parse results
	private void scanLeDevice() {

		// Stops scanning after a pre-defined scan period.
		if(!mScanning){
			Handler mHandler = new Handler();
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "stop scanning");
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);

					Log.v(TAG, "closing scan. beacon found:" + beaconFound);
					broadcastLocalUpdate();
					beaconFound = false;
					stopSelf();
				}
			}, SCAN_PERIOD);
			
			proximity=null;
			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
			Log.d(TAG, "start scanning");		
		}	
	}

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		private byte[] PDU;

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {

			PDU = scanRecord;		
            iBeacon res = db.getMatchingBeacon(PDU);

			//check if device is a beacon
			if (res!=null) {
				Log.v(TAG, "Beacon detected");
				beaconFound = true;
				res.setRSSI(rssi);
				
				//check whether this device is closer than previous devices.
				//override proximity if so.
				if(proximity==null){
					proximity = res;
				}else{
					if(proximity.getRSSI()< rssi){
						proximity = res;
					}
				}
			}
		}
	};

	// characters used to display hexadecimal numbers
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	// convert byte array to hexadecimal string
	public String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}