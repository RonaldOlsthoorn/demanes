package nl.senseos.mytimeatsense.bluetooth;

import java.util.Arrays;
import nl.senseos.mytimeatsense.storage.DBHelper;
import nl.senseos.mytimeatsense.util.ByteConverter;
import nl.senseos.mytimeatsense.util.Hex;

import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.database.Cursor;

public class iBeacon{

    public static final String IBEACON_PREAMBLE = "0201061AFF4C000215";
    private static String TAG = iBeacon.class.getSimpleName();
    private BluetoothDevice device;
    private long localId=-1;
    private long remoteId=-1;
    private String state;
    private String name;
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
    public static iBeacon parseAd(BluetoothDevice device, byte[] PDU) {

        String uuid;
        int major;
        int minor;
        int tx;

        if (!Hex.bytesToHex(Arrays.copyOfRange(PDU, 0, 9)).equals(IBEACON_PREAMBLE)) {
            return null;
        } else {
            uuid = Hex.bytesToHex(Arrays.copyOfRange(PDU, 9, 25));
            major = ByteConverter.bytesToUnsignedInt(Arrays.copyOfRange(PDU, 25, 27));
            minor = ByteConverter.bytesToUnsignedInt(Arrays.copyOfRange(PDU, 27, 29));
            tx = ByteConverter.bytesToUnsignedInt(new byte[]{PDU[29]});
        }

        return new iBeacon(device.getName(), uuid, major, minor, tx);
    }

    public static iBeacon getiBeacon(DBHelper db, int localId){

        Cursor c = db.getBeacon(localId);
        if(c.getCount()==0){
            return null;
        }
        c.moveToFirst();

        return new iBeacon(c.getInt(0), c.getInt(6), c.getString(1),
                c.getString(2), c.getInt(3), c.getInt(4), c.getInt(5));
    }

    public iBeacon(String name, String uuid, int major, int minor, int tx) {

        this.name = name;
        UUID = uuid;
        this.major = major;
        this.minor = minor;
        this.tx = tx;
        state = DBHelper.BeaconTable.STATE_ACTIVE;
    }

    public iBeacon(int localId, int remoteId, String name,
                   String uuid, int major, int minor, int tx) {

        this.localId = localId;
        this.remoteId = remoteId;
        this.name = name;
        UUID = uuid;
        this.major = major;
        this.minor = minor;
        this.tx = tx;
        state = DBHelper.BeaconTable.STATE_ACTIVE;
    }

    public long getLocalId(){
        return localId;
    }

    public void setLocalId(long id){
        localId = id;
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

    public int getTx(){
        return tx;
    }

    public void setTx(int tx){
        this.tx = tx;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState(){return state;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        beacon.put(DBHelper.BeaconTable.COLUMN_NAME_NAME, name);
        beacon.put(DBHelper.BeaconTable.COLUMN_NAME_UUID, UUID);
        beacon.put(DBHelper.BeaconTable.COLUMN_NAME_MAJOR, major);
        beacon.put(DBHelper.BeaconTable.COLUMN_NAME_MINOR, minor);
        beacon.put(DBHelper.BeaconTable.COLUMN_NAME_TX, tx);
        beacon.put(DBHelper.BeaconTable.COLUMN_NAME_REMOTE_ID, remoteId);
        beacon.put(DBHelper.BeaconTable.COLUMN_NAME_STATE, DBHelper.BeaconTable.STATE_ACTIVE);

        return localId = db.insertOrIgnore(DBHelper.BeaconTable.TABLE_NAME, beacon);
    }

    public boolean updateDB(DBHelper db){

        if(localId==-1){
            return false;
        }

        ContentValues beacon = new ContentValues();
        beacon.put(DBHelper.BeaconTable.COLUMN_NAME_NAME, name);
        beacon.put(DBHelper.BeaconTable.COLUMN_NAME_UUID, UUID);
        beacon.put(DBHelper.BeaconTable.COLUMN_NAME_MAJOR, major);
        beacon.put(DBHelper.BeaconTable.COLUMN_NAME_MINOR, minor);
        beacon.put(DBHelper.BeaconTable.COLUMN_NAME_TX, tx);
        beacon.put(DBHelper.BeaconTable.COLUMN_NAME_REMOTE_ID, remoteId);

        return (db.updateOrIgnore(DBHelper.BeaconTable.TABLE_NAME, localId, beacon));
    }

    public void deleteDB(DBHelper db){

        db.markOrDelete(this);
    }
}
