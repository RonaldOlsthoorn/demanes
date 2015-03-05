package nl.senseos.mytimeatsense.bluetooth;

import java.nio.ByteBuffer;
import java.util.Arrays;
import nl.senseos.mytimeatsense.util.Hex;
import android.util.Log;

public class iBeacon {

	private static final String IBEACON_PREAMBLE = "0201061AFF4C000215";
	private static final String PROXIMITY_UUID = "A0B137303A9A11E3AA6E0800200C9A66";
	private static String TAG = iBeacon.class.getSimpleName();

	private String uuid;
	private int major;
	private int minor;
	private int tx;
	private int rssi;

	/**
	 * returns beacon instance from PDU if parseable. If not, returns null.
	 * 
	 * @param PDU
	 *            advertisement byte array
	 */
	public static iBeacon parseAd(byte[] PDU) {

		String uuid;
		int major;
		int minor;
		int tx;

		if (!Hex.bytesToHex(Arrays.copyOfRange(PDU, 0, 9)).equals(IBEACON_PREAMBLE)) {
			return null;
		} else {
			uuid = Hex.bytesToHex(Arrays.copyOfRange(PDU, 9, 25));
			major = (int) ByteBuffer.wrap(Arrays.copyOfRange(PDU, 24, 26))
					.getShort();
			minor = (int) ByteBuffer.wrap(Arrays.copyOfRange(PDU, 26, 28))
					.getShort();
			tx = (int) PDU[29];
		}
		
		Log.d(TAG,"uuid: "+uuid);

		return new iBeacon(uuid, major, minor, tx);
	}

	/**
	 * returns beacon instance from PDU if parseable and supported. If not,
	 * returns null.
	 * 
	 * @param PDU
	 *            advertisement byte array
	 */
	public static iBeacon parseSupportedAd(byte[] PDU) {

		iBeacon res = parseAd(PDU);
		if (res == null) {
			return null;
		}
		if (res.getUUID().equals(PROXIMITY_UUID)) {
			return res;
		}
		return null;
	}

	public iBeacon(String uuid, int major, int minor, int tx) {

		this.uuid = uuid;
		this.major = major;
		this.minor = minor;
		this.tx = tx;
	}

    /**
     * @return uuid of beacon instance
     */
	public String getUUID() {
		return uuid;
	}

    /**
     * @return major of beacon instance
     */
	public int getMajor() {
		return major;
	}

    /**
     * @return minor of beacon instance
     */
	public int getMinor() {
		return minor;
	}

    /**
     * @return RSSI of the beacon at detection
     */
	public int getRSSI() {
		return rssi;
	}

    /**
     * Update rssi of beacon.
     * @param r
     */
	public void setRSSI(int r){
		rssi=r;
	}
}
