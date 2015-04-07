package nl.senseos.mytimeatsense.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ronald on 3-4-15.
 */
public class SmartDevice implements Parcelable{

    private int key;
    private boolean occupied;

    public SmartDevice(int key, boolean occupied){

        this.key = key;
        this.occupied =  occupied;

    }

    public SmartDevice(Parcel source){

        key = source.readInt();
        occupied = source.readByte() !=0;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(key);
        dest.writeByte((byte) (occupied?1:0));

    }

    public static final Parcelable.Creator<SmartDevice> CREATOR = new Creator<SmartDevice>() {
        public SmartDevice createFromParcel(Parcel source) {
            return new SmartDevice(source);
        }

        public SmartDevice[] newArray(int size) {
            return new SmartDevice[size];
        }
    };
}
