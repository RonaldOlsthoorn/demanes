package nl.senseos.mytimeatsense.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ronald on 1-4-15.
 */
public class SmartBike extends SmartDevice{

    public SmartBike(int k, boolean o){
        super(k, o);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected SmartBike(Parcel source){
        super(source.readInt(), source.readByte()!=0);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Parcelable.Creator<SmartBike> CREATOR = new Creator<SmartBike>() {
        public SmartBike createFromParcel(Parcel source) {
            return new SmartBike(source);
        }

        public SmartBike[] newArray(int size) {
            return new SmartBike[size];
        }
    };

}
