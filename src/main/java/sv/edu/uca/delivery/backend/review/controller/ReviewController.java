package sv.edu.uca.delivery.backend.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.uca.delivery.backend.review.dto.CreateReviewRequest;
import sv.edu.uca.delivery.backend.review.dto.ReviewResponse;
import sv.edu.uca.delivery.backend.review.service.ReviewService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@RequestBody @Valid CreateReviewRequest request) {
        return reviewService.create(request);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public List<ReviewResponse> byRestaurant(@PathVariable UUID restaurantId) {
        return reviewService.byRestaurant(restaurantId);
    }
}
