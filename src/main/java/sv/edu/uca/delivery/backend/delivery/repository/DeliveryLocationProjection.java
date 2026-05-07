package sv.edu.uca.delivery.backend.delivery.repository;

import java.time.LocalDateTime;

public interface DeliveryLocationProjection {

    Double getLatitude();

    Double getLongitude();

    Double getDistanceToDestinationMeters();

    LocalDateTime getRecordedAt();
}
