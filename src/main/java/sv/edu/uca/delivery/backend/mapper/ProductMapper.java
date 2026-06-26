package sv.edu.uca.delivery.backend.mapper;

import sv.edu.uca.delivery.backend.dto.ProductResponseDTO;
import sv.edu.uca.delivery.backend.entity.Product;
import sv.edu.uca.delivery.backend.entity.Promotion;

import java.math.BigDecimal;

public class ProductMapper {

    public static ProductResponseDTO toDTO(Product product, Promotion promotion) {

        BigDecimal discountedPrice = product.getPrice();

        Integer discountPercentage = 0;

        boolean promotionActive = false;

        if (promotion != null) {

            promotionActive = true;

            discountPercentage =
                    promotion.getDiscountPercentage();

            BigDecimal discount =
                    product.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(discountPercentage)
                                            .divide(BigDecimal.valueOf(100))
                            );

            discountedPrice =
                    product.getPrice().subtract(discount);
        }





        return ProductResponseDTO.builder()
                .id(product.getId())
                .restaurantId(product.getRestaurant().getId())

                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .imageUrl(product.getImageUrl())

                .name(product.getName())
                .description(product.getDescription())

                .price(product.getPrice())

                .discountPercentage(discountPercentage)

                .discountedPrice(discountedPrice)

                .promotionActive(promotionActive)

                .available(product.isAvailable())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
