package sv.edu.uca.delivery.backend;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryBackendApplicationTests {

    @Test
    void applicationCanBeInstantiated() {
        assertThat(new DeliveryBackendApplication()).isNotNull();
    }

}
