package sv.edu.uca.delivery.backend.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.uca.delivery.backend.product.entity.Product;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByRestaurantId(UUID restaurantId);

    List<Product> findByAvailableTrue();

    List<Product> findByCategoryId(Long CategoryId);

    @Query("""
            select p
            from Product p
            where p.available = true
              and (
                    lower(p.name) like lower(concat('%', :query, '%'))
                 or lower(coalesce(p.description, '')) like lower(concat('%', :query, '%'))
                 or lower(p.restaurant.name) like lower(concat('%', :query, '%'))
                 or lower(p.category.name) like lower(concat('%', :query, '%'))
              )
            order by p.name asc
            """)
    List<Product> searchAvailable(@Param("query") String query);
}
