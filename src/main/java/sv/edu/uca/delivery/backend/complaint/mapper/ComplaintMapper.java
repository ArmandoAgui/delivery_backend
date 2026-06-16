package sv.edu.uca.delivery.backend.complaint.mapper;

import org.springframework.stereotype.Component;
import sv.edu.uca.delivery.backend.complaint.dto.ComplaintResponse;
import sv.edu.uca.delivery.backend.complaint.dto.RefundResponse;
import sv.edu.uca.delivery.backend.complaint.entity.Complaint;
import sv.edu.uca.delivery.backend.complaint.entity.Refund;

import java.util.Optional;

@Component
public class ComplaintMapper {

    public ComplaintResponse toResponse(Complaint complaint, Optional<Refund> refund) {
        return new ComplaintResponse(
                complaint.getId(),
                complaint.getOrder().getId(),
                complaint.getCustomer().getId(),
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
