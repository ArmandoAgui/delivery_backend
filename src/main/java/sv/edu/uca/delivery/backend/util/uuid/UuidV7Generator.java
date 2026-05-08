package sv.edu.uca.delivery.backend.util.uuid;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.UUID;

public final class UuidV7Generator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidV7Generator() {
    }

    public static UUID generate() {
        return generate(Clock.systemUTC());
    }

    static UUID generate(Clock clock) {
        long timestampMillis = clock.millis();
        byte[] randomBytes = new byte[10];
        RANDOM.nextBytes(randomBytes);

        int randomA = ((randomBytes[0] & 0xFF) << 4) | ((randomBytes[1] & 0xF0) >>> 4);
        long randomB = ByteBuffer.wrap(randomBytes, 2, 8).getLong() & 0x3FFFFFFFFFFFFFFFL;

        long mostSignificantBits = ((timestampMillis & 0xFFFFFFFFFFFFL) << 16)
                | 0x7000L
                | randomA;

        long leastSignificantBits = 0x8000000000000000L
                | randomB;

        return new UUID(mostSignificantBits, leastSignificantBits);
    }
}
