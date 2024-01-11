package net.burningtnt.hmclfetcher.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class DigestUtils {
    private DigestUtils() {
    }

    private static final byte[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static String encode(byte[] hash) {
        int l = hash.length;
        byte[] out = new byte[l << 1];

        for (int i = 0; i < l; i++) {
            byte b = hash[i];

            int j = i << 1;
            out[j] = DIGITS_LOWER[((0xF0 & b) >>> 4)];
            out[j + 1] = DIGITS_LOWER[(0xF & b)];
        }
        return new String(out, StandardCharsets.UTF_8);
    }

    public static String digest(String data) throws NoSuchAlgorithmException {
        return encode(MessageDigest.getInstance("SHA-1").digest(data.getBytes(StandardCharsets.UTF_8)));
    }

    public static String digest(String... data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        for (String d : data) {
            digest.update(d.getBytes(StandardCharsets.UTF_8));
        }
        return encode(digest.digest());
    }
}
