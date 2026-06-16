package sv.edu.uca.delivery.backend.loyalty.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.uca.delivery.backend.loyalty.dto.LoyaltyResponse;
import sv.edu.uca.delivery.backend.loyalty.dto.RedeemPointsRequest;
import sv.edu.uca.delivery.backend.loyalty.service.LoyaltyService;

@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @GetMapping
    public LoyaltyResponse mine() {
        return loyaltyService.mine();
    }

    @PostMapping("/redeem")
    public LoyaltyResponse redeem(@RequestBody @Valid RedeemPointsRequest request) {
        return loyaltyService.redeem(request);
    }
}
