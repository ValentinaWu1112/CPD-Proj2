package crypto;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public final class Crypto {

    private Crypto(){}

    public static String encodeValue(String value){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return new String(hash, StandardCharsets.US_ASCII);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
