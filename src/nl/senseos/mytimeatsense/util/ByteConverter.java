package nl.senseos.mytimeatsense.util;

import java.nio.ByteBuffer;

/**
 * Created by ronald on 6-3-15.
 */
public class ByteConverter {

    public static int bytesToUnsignedInt(byte[] bytes){

        if (bytes.length>2){
            return -1;
        }

        ByteBuffer b = ByteBuffer.allocate(4);
        b.put(bytes);
        int res = b.getInt(0)>>> 16;
        return res;
    }
}
