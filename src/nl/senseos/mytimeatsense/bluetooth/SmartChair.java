package nl.senseos.mytimeatsense.bluetooth;

/**
 * Created by ronald on 26-3-15.
 */
public class SmartChair {

    private iBeacon beacon;

    public SmartChair(iBeacon b){

        beacon = b;
    }

    public iBeacon getBeacon() {
        return beacon;
    }
}
