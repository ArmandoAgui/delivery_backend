package sv.edu.uca.delivery.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class RestaurantScheduleDTO {

    private Long id;

    @Min(1)
    @Max(7)
    private int dayOfWeek;

    private LocalTime opensAt;

    private LocalTime closesAt;

    private boolean closed;
}
