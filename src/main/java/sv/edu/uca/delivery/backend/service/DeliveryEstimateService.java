package sv.edu.uca.delivery.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sv.edu.uca.delivery.backend.entity.Address;
import sv.edu.uca.delivery.backend.entity.Cart;
import sv.edu.uca.delivery.backend.dto.DeliveryEstimate;
import sv.edu.uca.delivery.backend.entity.Restaurant;
import sv.edu.uca.delivery.backend.repository.RestaurantRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeliveryEstimateService {

    private static final BigDecimal BASE_FEE = new BigDecimal("1.10");
    private static final BigDecimal FALLBACK_DISTANCE_KM = new BigDecimal("4.00");
    private static final BigDecimal PEAK_MULTIPLIER = new BigDecimal("1.10");
    private static final int BASE_MINUTES = 20;
    private static final int MINUTES_PER_KM = 4;

    private final RestaurantRepository restaurantRepository;

    public DeliveryEstimate estimate(Restaurant restaurant, Address address, int itemCount) {
        BigDecimal distanceKm = distanceKm(restaurant, address);
        boolean peak = isPeakDemand();
        BigDecimal multiplier = peak ? PEAK_MULTIPLIER : BigDecimal.ONE;
        BigDecimal fee = deliveryFeeForDistance(distanceKm)
                .multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);
        int minutes = BASE_MINUTES
                + distanceKm.multiply(BigDecimal.valueOf(MINUTES_PER_KM)).setScale(0, RoundingMode.CEILING).intValue()
                + Math.max(0, itemCount - 1) * 2
                + (restaurant.isOpen() ? 0 : 10)
                + (peak ? 8 : 0);
        return new DeliveryEstimate(fee, minutes, peak, distanceKm, multiplier);
    }

    public DeliveryEstimate estimateForCart(Cart cart, Optional<Address> address) {
        int itemCount = cart.getItems().stream().mapToInt(item -> item.getQuantity()).sum();
        return address
                .map(value -> estimate(cart.getRestaurant(), value, itemCount))
                .orElseGet(() -> fallback(itemCount, cart.getRestaurant().isOpen()));
    }

    private DeliveryEstimate fallback(int itemCount, boolean restaurantOpen) {
        boolean peak = isPeakDemand();
        BigDecimal multiplier = peak ? PEAK_MULTIPLIER : BigDecimal.ONE;
        BigDecimal fee = deliveryFeeForDistance(FALLBACK_DISTANCE_KM)
                .multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);
        int minutes = BASE_MINUTES + 16 + Math.max(0, itemCount - 1) * 2 + (restaurantOpen ? 0 : 10) + (peak ? 8 : 0);
        return new DeliveryEstimate(fee, minutes, peak, FALLBACK_DISTANCE_KM, multiplier);
    }

    private BigDecimal deliveryFeeForDistance(BigDecimal distanceKm) {
        BigDecimal remaining = distanceKm.max(BigDecimal.ZERO);
        BigDecimal fee = BASE_FEE;

        BigDecimal firstTier = remaining.min(new BigDecimal("3.00"));
        fee = fee.add(firstTier.multiply(new BigDecimal("0.35")));
        remaining = remaining.subtract(firstTier);

        if (remaining.signum() > 0) {
            BigDecimal secondTier = remaining.min(new BigDecimal("3.00"));
            fee = fee.add(secondTier.multiply(new BigDecimal("0.45")));
            remaining = remaining.subtract(secondTier);
        }

        if (remaining.signum() > 0) {
            BigDecimal thirdTier = remaining.min(new BigDecimal("4.00"));
            fee = fee.add(thirdTier.multiply(new BigDecimal("0.55")));
            remaining = remaining.subtract(thirdTier);
        }

        if (remaining.signum() > 0) {
            fee = fee.add(remaining.multiply(new BigDecimal("0.65")));
        }

        return fee;
    }

    private BigDecimal distanceKm(Restaurant restaurant, Address address) {
        Double distance = restaurantRepository.distanceKmBetweenRestaurantAndAddress(restaurant.getId(), address.getId());
        if (distance == null || distance.isNaN() || distance.isInfinite()) {
            return FALLBACK_DISTANCE_KM;
        }
        return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isPeakDemand() {
        LocalTime now = LocalTime.now();
        return (!now.isBefore(LocalTime.of(11, 30)) && now.isBefore(LocalTime.of(13, 30)))
                || (!now.isBefore(LocalTime.of(18, 0)) && now.isBefore(LocalTime.of(20, 30)));
    }
}
