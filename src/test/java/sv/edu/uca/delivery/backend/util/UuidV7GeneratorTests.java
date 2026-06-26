package sv.edu.uca.delivery.backend.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class UuidV7GeneratorTests {

    @Test
    void generateReturnsUuidVersion7WithRfc4122Variant() {
        UUID uuid = UuidV7Generator.generate();

        assertThat(uuid.version()).isEqualTo(7);
        assertThat(uuid.variant()).isEqualTo(2);
    }
}
