package sv.edu.uca.delivery.backend.mapper;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import sv.edu.uca.delivery.backend.dto.RestaurantScheduleDTO;
import sv.edu.uca.delivery.backend.dto.RestaurantResponseDTO;
import sv.edu.uca.delivery.backend.entity.Restaurant;
import sv.edu.uca.delivery.backend.entity.RestaurantSchedule;

public class RestaurantMapper {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    public static RestaurantResponseDTO toDTO(Restaurant restaurant) {
        return toDTO(restaurant, restaurant.isOpen());
    }

    public static RestaurantResponseDTO toDTO(Restaurant restaurant, boolean open) {
        return toDTO(restaurant, open, null, 0);
    }

    public static RestaurantResponseDTO toDTO(Restaurant restaurant, boolean open, Double averageRating, long reviewCount) {
        Point location = restaurant.getLocation();
        return RestaurantResponseDTO.builder()
                .id(restaurant.getId())
                .ownerId(restaurant.getOwner().getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .phone(restaurant.getPhone())
                .email(restaurant.getEmail())
                .streetAddress(restaurant.getStreetAddress())
                .department(restaurant.getDepartment())
                .latitude(location == null ? null : location.getY())
                .longitude(location == null ? null : location.getX())
                .imageUrl(restaurant.getImageUrl())
                .open(open)
                .active(restaurant.isActive())
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .createdAt(restaurant.getCreatedAt())
                .build();
    }

    public static RestaurantScheduleDTO toDTO(RestaurantSchedule schedule) {
        return RestaurantScheduleDTO.builder()
                .id(schedule.getId())
                .dayOfWeek(schedule.getDayOfWeek())
                .opensAt(schedule.getOpensAt())
                .closesAt(schedule.getClosesAt())
                .closed(schedule.isClosed())
                .build();
    }

    public static Point toLocation(double latitude, double longitude) {
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);
        return point;
    }
}
