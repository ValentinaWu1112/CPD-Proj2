package crypto;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;

public final class Crypto {

    private Crypto(){}

    public static String encodeValue(String value){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, hash);
            StringBuilder hex_string = new StringBuilder(number.toString(16));
            while (hex_string.length() < 64) hex_string.insert(0, '0');
            return hex_string.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
