package sv.edu.uca.delivery.backend.loyalty.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.common.exception.BusinessException;
import sv.edu.uca.delivery.backend.loyalty.dto.LoyaltyResponse;
import sv.edu.uca.delivery.backend.loyalty.dto.LoyaltyTransactionResponse;
import sv.edu.uca.delivery.backend.loyalty.dto.RedeemPointsRequest;
import sv.edu.uca.delivery.backend.loyalty.entity.LoyaltyAccount;
import sv.edu.uca.delivery.backend.loyalty.entity.LoyaltyTransaction;
import sv.edu.uca.delivery.backend.loyalty.entity.LoyaltyTransactionType;
import sv.edu.uca.delivery.backend.loyalty.repository.LoyaltyAccountRepository;
import sv.edu.uca.delivery.backend.loyalty.repository.LoyaltyTransactionRepository;
import sv.edu.uca.delivery.backend.order.entity.Order;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public LoyaltyResponse mine() {
        return toResponse(getOrCreateAccount(authenticatedUserProvider.getCurrentUserId()));
    }

    @Transactional
    public LoyaltyResponse redeem(RedeemPointsRequest request) {
        LoyaltyAccount account = getOrCreateAccount(authenticatedUserProvider.getCurrentUserId());
        if (account.getPointsBalance() < request.points()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Not enough loyalty points");
        }
        account.setPointsBalance(account.getPointsBalance() - request.points());
        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setAccount(account);
        transaction.setTransactionType(LoyaltyTransactionType.REDEEMED);
        transaction.setPoints(-request.points());
        transaction.setDescription("Basic points redemption");
        transactionRepository.save(transaction);
        return toResponse(accountRepository.save(account));
    }

    @Transactional
    public void awardForDeliveredOrder(Order order) {
        if (transactionRepository.existsByOrderIdAndTransactionType(order.getId(), LoyaltyTransactionType.EARNED)) {
            return;
        }
        int points = order.getTotalAmount().setScale(0, RoundingMode.DOWN).intValue();
        if (points <= 0) {
            return;
        }
        LoyaltyAccount account = getOrCreateAccount(order.getCustomer().getId());
        account.setPointsBalance(account.getPointsBalance() + points);
        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setAccount(account);
        transaction.setOrder(order);
        transaction.setTransactionType(LoyaltyTransactionType.EARNED);
        transaction.setPoints(points);
        transaction.setDescription("Points earned from delivered order");
        accountRepository.save(account);
        transactionRepository.save(transaction);
    }

    private LoyaltyAccount getOrCreateAccount(java.util.UUID customerId) {
        return accountRepository.findByCustomerId(customerId).orElseGet(() -> {
            User customer = userRepository.findByIdAndActiveTrue(customerId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Customer not found"));
            LoyaltyAccount account = new LoyaltyAccount();
            account.setCustomer(customer);
            return accountRepository.save(account);
        });
    }

    private LoyaltyResponse toResponse(LoyaltyAccount account) {
        return new LoyaltyResponse(
                account.getCustomer().getId(),
                account.getPointsBalance(),
                transactionRepository.findByAccountCustomerIdOrderByCreatedAtDesc(account.getCustomer().getId())
                        .stream()
                        .map(tx -> new LoyaltyTransactionResponse(
                                tx.getId(),
                                tx.getTransactionType(),
                                tx.getPoints(),
                                tx.getDescription(),
                                tx.getCreatedAt()
                        ))
                        .toList()
        );
    }
}
