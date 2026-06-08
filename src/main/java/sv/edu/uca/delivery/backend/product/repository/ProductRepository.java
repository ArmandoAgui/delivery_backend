package sv.edu.uca.delivery.backend.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.uca.delivery.backend.product.entity.Product;
import sv.edu.uca.delivery.backend.product.entity.ProductCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByActiveTrue();

    Optional<Product> findByIdAndActiveTrue(UUID id);

    List<Product> findByRestaurantIdAndActiveTrue(UUID restaurantId);

    List<Product> findByCategoryAndActiveTrue(ProductCategory category);

    List<Product> findByAvailableTrueAndActiveTrue();
}