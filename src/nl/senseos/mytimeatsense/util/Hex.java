package nl.senseos.mytimeatsense.util;

/**
 * Created by ronald on 26-2-15.
 */
public class Hex {

    // characters used to display hexadecimal numbers
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private String hex;
    private byte[] bytes;

    // convert byte array to hexadecimal string
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public Hex(byte[] bytes){

        this.bytes= bytes;
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        hex = new String(hexChars);
    }

    public byte[] getBytes(){
        return bytes;
    }

    public String toString(){
        return hex;
    }
}
