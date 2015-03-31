package nl.senseos.mytimeatsense.bluetooth;

import android.bluetooth.BluetoothDevice;

import java.util.Arrays;
import nl.senseos.mytimeatsense.util.ByteConverter;
import nl.senseos.mytimeatsense.util.Hex;

/**
 * Created by ronald on 26-3-15.
 */
public class SmartDesk {

    private int id;
    private SmartChair chair;

    public static SmartDesk createDesk(BluetoothDevice device, byte[] PDU){

        String uuid;
        int major;
        int minor;
        int tx;
        int deskId;

        if (!Hex.bytesToHex(Arrays.copyOfRange(PDU, 0, 9)).equals(iBeacon.IBEACON_PREAMBLE)) {
            return null;
        } else {
            uuid = Hex.bytesToHex(Arrays.copyOfRange(PDU, 9, 25));
            major = ByteConverter.bytesToUnsignedInt(Arrays.copyOfRange(PDU, 25, 27));
            minor = ByteConverter.bytesToUnsignedInt(Arrays.copyOfRange(PDU, 27, 29));
            tx = ByteConverter.bytesToUnsignedInt(new byte[]{PDU[29]});
            deskId = ByteConverter.bytesToUnsignedInt(new byte[]{PDU[30]});
        }

        SmartDesk res = new SmartDesk(deskId);
        res.setChair(new SmartChair(new iBeacon(device.getName(), uuid, major, minor, tx)));

        return res;
    }

    public SmartDesk(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public void setChair(SmartChair chair) {
        this.chair = chair;
    }

    public SmartChair getChair() {
        return chair;
    }
}
