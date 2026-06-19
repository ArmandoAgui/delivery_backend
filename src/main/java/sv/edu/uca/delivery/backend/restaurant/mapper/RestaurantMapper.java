package sv.edu.uca.delivery.backend.restaurant.mapper;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import sv.edu.uca.delivery.backend.restaurant.dto.RestaurantScheduleDTO;
import sv.edu.uca.delivery.backend.restaurant.dto.response.RestaurantResponseDTO;
import sv.edu.uca.delivery.backend.restaurant.entity.Restaurant;
import sv.edu.uca.delivery.backend.restaurant.entity.RestaurantSchedule;

public class RestaurantMapper {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    public static RestaurantResponseDTO toDTO(Restaurant restaurant) {
        return toDTO(restaurant, restaurant.isOpen());
    }

    public static RestaurantResponseDTO toDTO(Restaurant restaurant, boolean open) {
        Point location = restaurant.getLocation();
        return RestaurantResponseDTO.builder()
                .id(restaurant.getId())
                .ownerId(restaurant.getOwner().getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .phone(restaurant.getPhone())
                .email(restaurant.getEmail())
                .streetAddress(restaurant.getStreetAddress())
                .city(restaurant.getCity())
                .state(restaurant.getState())
                .country(restaurant.getCountry())
                .latitude(location == null ? null : location.getY())
                .longitude(location == null ? null : location.getX())
                .imageUrl(restaurant.getImageUrl())
                .open(open)
                .active(restaurant.isActive())
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
