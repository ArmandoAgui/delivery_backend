package sv.edu.uca.delivery.backend.util.uuid;

import java.security.SecureRandom;
import java.util.UUID;

public final class UuidV7Generator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidV7Generator() {
    }

    public static UUID next() {
        long timestamp = System.currentTimeMillis();
        byte[] randomBytes = new byte[10];
        RANDOM.nextBytes(randomBytes);

        long mostSignificantBits = 0L;
        mostSignificantBits |= (timestamp & 0xFFFFFFFFFFFFL) << 16;
        mostSignificantBits |= 0x7000L;
        mostSignificantBits |= ((long) randomBytes[0] & 0xFFL) << 8;
        mostSignificantBits |= (long) randomBytes[1] & 0xFFL;

        long leastSignificantBits = 0L;
        leastSignificantBits |= ((long) randomBytes[2] & 0x3FL) << 56;
        leastSignificantBits |= 0x8000000000000000L;
        for (int i = 3; i < randomBytes.length; i++) {
            leastSignificantBits |= ((long) randomBytes[i] & 0xFFL) << ((9 - i) * 8);
        }

        return new UUID(mostSignificantBits, leastSignificantBits);
    }
}
