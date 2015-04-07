package nl.senseos.mytimeatsense.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ronald on 26-3-15.
 */
public class SmartChair extends SmartDevice{

    private SmartDesk desk;

    public SmartChair(int key, boolean occupied){
        super(key, occupied);

    }

    public SmartDesk getDesk() {
        return desk;
    }

    public void setDesk(SmartDesk desk) {
        this.desk = desk;
    }

    protected SmartChair(Parcel source){
        super(source.readInt(), source.readByte()!=0);
        desk = source.readParcelable(SmartDesk.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        super.writeToParcel(dest, flags);
        dest.writeParcelable(desk, flags);
    }

    public static final Parcelable.Creator<SmartChair> CREATOR = new Creator<SmartChair>() {
        public SmartChair createFromParcel(Parcel source) {
            return new SmartChair(source);
        }

        public SmartChair[] newArray(int size) {
            return new SmartChair[size];
        }
    };
}
