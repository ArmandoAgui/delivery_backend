package sv.edu.uca.delivery.backend.restaurant.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class RestaurantScheduleRequestDTO {

    @Min(1)
    @Max(7)
    private int dayOfWeek;

    private LocalTime opensAt;

    private LocalTime closesAt;

    private boolean closed;
}
