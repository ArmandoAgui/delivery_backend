package sv.edu.uca.delivery.backend.common.time;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class AppClock {

    public static final ZoneId BUSINESS_ZONE = ZoneId.of("America/El_Salvador");

    private AppClock() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(BUSINESS_ZONE);
    }
}
