package sv.edu.uca.delivery.backend.complaint.mapper;

import org.springframework.stereotype.Component;
import sv.edu.uca.delivery.backend.complaint.dto.ComplaintResponse;
import sv.edu.uca.delivery.backend.complaint.dto.RefundResponse;
import sv.edu.uca.delivery.backend.complaint.entity.Complaint;
import sv.edu.uca.delivery.backend.complaint.entity.Refund;
import sv.edu.uca.delivery.backend.restaurant.entity.Restaurant;

import java.util.Optional;

@Component
public class ComplaintMapper {

    public ComplaintResponse toResponse(Complaint complaint, Optional<Refund> refund) {
        Restaurant restaurant = complaint.getOrder().getRestaurant();
        return new ComplaintResponse(
                complaint.getId(),
                complaint.getOrder().getId(),
                complaint.getCustomer().getId(),
                complaint.getCustomer().getFirstName() + " " + complaint.getCustomer().getLastName(),
                complaint.getCustomer().getEmail(),
                restaurant == null ? null : restaurant.getId(),
                restaurant == null ? null : restaurant.getName(),
                complaint.getOrder().getStatus().name(),
                complaint.getStatus(),
                complaint.getSubject(),
                complaint.getDescription(),
                complaint.getResolution(),
                complaint.getCreatedAt(),
                refund.map(this::toRefundResponse).orElse(null)
        );
    }

    private RefundResponse toRefundResponse(Refund refund) {
        return new RefundResponse(
                refund.getId(),
                true,
                refund.getStatus(),
                refund.getAmount(),
                refund.getReason(),
                refund.getProcessedAt()
        );
    }
}
