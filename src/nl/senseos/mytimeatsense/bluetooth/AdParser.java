package nl.senseos.mytimeatsense.bluetooth;

import android.bluetooth.BluetoothDevice;

import java.util.Arrays;

import nl.senseos.mytimeatsense.util.ByteConverter;
import nl.senseos.mytimeatsense.util.Hex;

/**
 * Created by ronald on 3-4-15.
 *
 */
public class AdParser {

    public static final String IBEACON_PREAMBLE = "0201061AFF4C000215";
    public static final String DEMANES_UUID = "28415B70D91511E488300800200C9A66";

    public static iBeacon buildBeacon(BluetoothDevice device, byte[] PDU){

        String uuid;
        int major;
        int minor;
        int tx;

        if (!Hex.bytesToHex(Arrays.copyOfRange(PDU, 0, 9)).equals(IBEACON_PREAMBLE)) {
            return null;
        }
        uuid = Hex.bytesToHex(Arrays.copyOfRange(PDU, 9, 25));
        major = ByteConverter.bytesToUnsignedInt(Arrays.copyOfRange(PDU, 25, 27));
        minor = ByteConverter.bytesToUnsignedInt(Arrays.copyOfRange(PDU, 27, 29));
        tx = ByteConverter.bytesToUnsignedInt(new byte[]{PDU[29]});

        iBeacon beacon = new iBeacon(device.getName(), uuid, major, minor, tx);

        if(beacon.getUUID().equals(DEMANES_UUID)){
            int key = major/10;

            if(key != 5){

                SmartChair chair = new SmartChair(key, (major-key*10)==1);
                beacon.setSmartDevice(chair);

                if(beacon.getMinor()!=0){
                    int deskKey = beacon.getMinor()/100;
                    int rssi = beacon.getMinor()-deskKey*100;
                    SmartDesk desk= new SmartDesk(deskKey, rssi);
                    chair.setDesk(desk);
                }
            }else{
                SmartBike bike = new SmartBike(key, (major-key*10)==1);
                beacon.setSmartDevice(bike); ;
            }
        }
        return beacon;
    }
}
