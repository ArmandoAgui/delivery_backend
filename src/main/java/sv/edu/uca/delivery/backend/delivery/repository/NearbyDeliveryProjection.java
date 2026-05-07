package sv.edu.uca.delivery.backend.delivery.repository;

import java.util.UUID;

public interface NearbyDeliveryProjection {

    UUID getDeliveryUserId();

    String getFullName();

    String getEmail();

    Double getDistanceMeters();
}
