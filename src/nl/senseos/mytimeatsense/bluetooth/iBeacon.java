package nl.senseos.mytimeatsense.bluetooth;

import java.nio.ByteBuffer;
import java.util.Arrays;
import nl.senseos.mytimeatsense.storage.DBHelper;
import nl.senseos.mytimeatsense.util.Hex;
import android.content.ContentValues;
import android.util.Log;

public class iBeacon {

    private static final String IBEACON_PREAMBLE = "0201061AFF4C000215";
    private static String TAG = iBeacon.class.getSimpleName();

    private long localId;
    private long remoteId;
    private long entityId;
    private String UUID;
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

    public iBeacon(String uuid, int major, int minor, int tx) {

        UUID = uuid;
        this.major = major;
        this.minor = minor;
        this.tx = tx;
    }

    /**
     * @return uuid of beacon instance
     */
    public String getUUID() {
        return UUID;
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
     *
     */
    public long getRemoteId(){
        return remoteId;
    }

    /**
     *
     * @param id
     */
    public void setRemoteId(long id){
        remoteId = id;
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


    public long insertDB(DBHelper db){

        ContentValues beacon = new ContentValues();
        beacon.put(DBHelper.BeaconTable.COLUMN_UUID, UUID);
        beacon.put(DBHelper.BeaconTable.COLUMN_MAJOR, major);
        beacon.put(DBHelper.BeaconTable.COLUMN_MINOR, minor);
        beacon.put(DBHelper.BeaconTable.COLUMN_REMOTE_ID, remoteId);

        return localId = db.insertOrIgnore(DBHelper.BeaconTable.TABLE_NAME, beacon);
    }

    public boolean updateDB(DBHelper db){

        if(localId==-1){
            return false;
        }

        ContentValues beacon = new ContentValues();
        beacon.put(DBHelper.BeaconTable.COLUMN_UUID, UUID);
        beacon.put(DBHelper.BeaconTable.COLUMN_MAJOR, major);
        beacon.put(DBHelper.BeaconTable.COLUMN_MINOR, minor);
        beacon.put(DBHelper.BeaconTable.COLUMN_REMOTE_ID, remoteId);

        return (db.updateOrIgnore(DBHelper.BeaconTable.TABLE_NAME, localId, beacon));
    }
}
