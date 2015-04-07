package nl.senseos.mytimeatsense.bluetooth;


import nl.senseos.mytimeatsense.storage.DBHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class iBeacon implements Parcelable {

    private static String TAG = iBeacon.class.getSimpleName();
    private long localId=-1;
    private String name;
    private String UUID;
    private int major;
    private int minor;
    private int tx;
    private int rssi;

    private SmartDevice smartDevice;

    public static iBeacon getiBeacon(DBHelper db, int localId){

        Cursor c = db.getBeacon(localId);
        if(c.getCount()==0){
            return null;
        }
        c.moveToFirst();

        return new iBeacon(c.getInt(DBHelper.BeaconTable.COLUMN_INDEX_ID),
                c.getString(DBHelper.BeaconTable.COLUMN_INDEX_NAME),
                c.getString(DBHelper.BeaconTable.COLUMN_INDEX_UUID),
                c.getInt(DBHelper.BeaconTable.COLUMN_INDEX_MAJOR),
                c.getInt(DBHelper.BeaconTable.COLUMN_INDEX_MINOR),
                c.getInt(DBHelper.BeaconTable.COLUMN_INDEX_TX));
    }

    public iBeacon(String name, String uuid, int major, int minor, int tx) {

        this.name = name;
        UUID = uuid;
        this.major = major;
        this.minor = minor;
        this.tx = tx;

    }

    public iBeacon(int localId, String name,
                   String uuid, int major, int minor, int tx) {

        this.localId = localId;
        this.name = name;
        UUID = uuid;
        this.major = major;
        this.minor = minor;
        this.tx = tx;

    }

    protected iBeacon(Parcel in) {

        localId = in.readLong();
        name = in.readString();
        UUID = in.readString();
        major = in.readInt();
        minor = in.readInt();
        tx = in.readInt();
        rssi = in.readInt();
        smartDevice = in.readParcelable(SmartDevice.class.getClassLoader());
    }

    public static final Parcelable.Creator<iBeacon> CREATOR = new Creator<iBeacon>() {
        public iBeacon createFromParcel(Parcel source) {
            return new iBeacon(source);
        }

        public iBeacon[] newArray(int size) {
            return new iBeacon[size];
        }
    };

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

    public int getTx(){
        return tx;
    }

    public void setTx(int tx){
        this.tx = tx;
    }

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

    public SmartDevice getSmartDevice() {
        return smartDevice;
    }

    public void setSmartDevice(SmartDevice smartDevice) {
        this.smartDevice = smartDevice;
    }

    public boolean hasSmartDevice(){

        return null!=smartDevice;
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

        return (db.updateOrIgnore(DBHelper.BeaconTable.TABLE_NAME, localId, beacon));
    }

    public void deleteDB(DBHelper db){

        db.deleteOrIgnore(DBHelper.BeaconTable.TABLE_NAME, localId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeLong(localId);
        dest.writeString(name);
        dest.writeString(UUID);
        dest.writeInt(major);
        dest.writeInt(minor);
        dest.writeInt(tx);
        dest.writeInt(rssi);
        dest.writeParcelable(smartDevice, flags);

    }
}
