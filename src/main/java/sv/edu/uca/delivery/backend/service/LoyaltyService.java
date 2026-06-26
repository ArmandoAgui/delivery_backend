package sv.edu.uca.delivery.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.exception.BusinessException;
import sv.edu.uca.delivery.backend.dto.LoyaltyResponse;
import sv.edu.uca.delivery.backend.dto.LoyaltyTransactionResponse;
import sv.edu.uca.delivery.backend.dto.RedeemPointsRequest;
import sv.edu.uca.delivery.backend.entity.LoyaltyAccount;
import sv.edu.uca.delivery.backend.entity.LoyaltyTransaction;
import sv.edu.uca.delivery.backend.entity.LoyaltyTransactionType;
import sv.edu.uca.delivery.backend.repository.LoyaltyAccountRepository;
import sv.edu.uca.delivery.backend.repository.LoyaltyTransactionRepository;
import sv.edu.uca.delivery.backend.entity.Order;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.entity.User;
import sv.edu.uca.delivery.backend.repository.UserRepository;

import java.math.RoundingMode;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private static final BigDecimal POINT_CREDIT_VALUE = new BigDecimal("0.01");

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
        BigDecimal creditAmount = creditForPoints(request.points());
        account.setPointsBalance(account.getPointsBalance() - request.points());
        account.setCreditBalance(creditBalance(account).add(creditAmount));
        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setAccount(account);
        transaction.setTransactionType(LoyaltyTransactionType.REDEEMED);
        transaction.setPoints(-request.points());
        transaction.setCreditAmount(creditAmount);
        transaction.setDescription("Points redeemed into digital wallet credit");
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
        transaction.setCreditAmount(BigDecimal.ZERO);
        transaction.setDescription("Points earned from delivered order");
        accountRepository.save(account);
        transactionRepository.save(transaction);
    }

    @Transactional
    public BigDecimal redeemAllForOrder(User customer, Order order, BigDecimal maximumDiscount) {
        LoyaltyAccount account = getOrCreateAccount(customer.getId());
        int points = account.getPointsBalance();
        if (points <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal creditValue = creditForPoints(points);
        BigDecimal discount = creditValue.min(maximumDiscount.max(BigDecimal.ZERO));
        account.setPointsBalance(0);
        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setAccount(account);
        transaction.setOrder(order);
        transaction.setTransactionType(LoyaltyTransactionType.REDEEMED);
        transaction.setPoints(-points);
        transaction.setCreditAmount(discount);
        transaction.setDescription("All loyalty points redeemed for order credit");
        accountRepository.save(account);
        transactionRepository.save(transaction);
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public BigDecimal applyDigitalCreditForOrder(User customer, Order order, BigDecimal maximumDiscount) {
        LoyaltyAccount account = getOrCreateAccount(customer.getId());
        BigDecimal availableCredit = account.getCreditBalance() == null ? BigDecimal.ZERO : account.getCreditBalance();
        BigDecimal discount = availableCredit.min(maximumDiscount.max(BigDecimal.ZERO)).setScale(2, RoundingMode.HALF_UP);
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        account.setCreditBalance(availableCredit.subtract(discount).setScale(2, RoundingMode.HALF_UP));
        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setAccount(account);
        transaction.setOrder(order);
        transaction.setTransactionType(LoyaltyTransactionType.REDEEMED);
        transaction.setPoints(0);
        transaction.setCreditAmount(discount.negate());
        transaction.setDescription("Digital wallet credit applied to order");
        accountRepository.save(account);
        transactionRepository.save(transaction);
        return discount;
    }

    @Transactional
    public void creditRefund(User customer, Order order, BigDecimal amount, String reason) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        LoyaltyAccount account = getOrCreateAccount(customer.getId());
        BigDecimal credit = amount.setScale(2, RoundingMode.HALF_UP);
        account.setCreditBalance(creditBalance(account).add(credit));
        LoyaltyTransaction transaction = new LoyaltyTransaction();
        transaction.setAccount(account);
        transaction.setOrder(order);
        transaction.setTransactionType(LoyaltyTransactionType.ADJUSTED);
        transaction.setPoints(0);
        transaction.setCreditAmount(credit);
        transaction.setDescription(reason == null || reason.isBlank() ? "Refund credited to digital wallet" : reason);
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
                creditForPoints(account.getPointsBalance()),
                creditBalance(account),
                creditBalance(account).add(creditForPoints(account.getPointsBalance())),
                transactionRepository.findByAccountCustomerIdOrderByCreatedAtDesc(account.getCustomer().getId())
                        .stream()
                        .map(tx -> new LoyaltyTransactionResponse(
                                tx.getId(),
                                tx.getTransactionType(),
                                tx.getPoints(),
                                tx.getCreditAmount(),
                                tx.getDescription(),
                                tx.getCreatedAt()
                        ))
                        .toList()
        );
    }

    private BigDecimal creditForPoints(int points) {
        return POINT_CREDIT_VALUE.multiply(BigDecimal.valueOf(points)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal creditBalance(LoyaltyAccount account) {
        return account.getCreditBalance() == null ? BigDecimal.ZERO : account.getCreditBalance();
    }
}
