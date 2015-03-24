package nl.senseos.mytimeatsense.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ronald on 24-3-15.
 */
public class Hash {

    /**
     * @param hashMe "clear" password String to be hashed before sending it to
     *               CommonSense
     * @return Hashed String
     */
    public static String hashMD5(String hashMe) {
        final byte[] unhashedBytes = hashMe.getBytes();
        try {
            final MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(unhashedBytes);
            final byte[] hashedBytes = algorithm.digest();

            final StringBuffer hexString = new StringBuffer();
            for (final byte element : hashedBytes) {
                final String hex = Integer.toHexString(0xFF & element);
                if (hex.length() == 1) {
                    hexString.append(0);
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
