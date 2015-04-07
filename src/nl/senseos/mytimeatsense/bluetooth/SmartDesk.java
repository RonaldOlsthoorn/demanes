package nl.senseos.mytimeatsense.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ronald on 26-3-15.
 */
public class SmartDesk implements Parcelable{

    private int key;
    private int rssi;

    public SmartDesk(int key, int rssi){
        this.key = key; this.rssi = rssi;
    }

    public int getRssi(){
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(key);
        dest.writeInt(rssi);
    }

    protected SmartDesk(Parcel source){

        key = source.readInt();
        rssi = source.readInt();
    }

    public static final Parcelable.Creator<SmartDesk> CREATOR = new Creator<SmartDesk>() {
        public SmartDesk createFromParcel(Parcel source) {
            return new SmartDesk(source);
        }

        public SmartDesk[] newArray(int size) {
            return new SmartDesk[size];
        }
    };
}
