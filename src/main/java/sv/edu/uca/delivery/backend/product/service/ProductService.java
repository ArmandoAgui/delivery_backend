package sv.edu.uca.delivery.backend.product.service;

import sv.edu.uca.delivery.backend.product.dto.ProductCreateDTO;
import sv.edu.uca.delivery.backend.product.dto.ProductUpdateDTO;
import sv.edu.uca.delivery.backend.product.dto.response.ProductResponseDTO;

import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

public interface ProductService {

    ProductResponseDTO create(ProductCreateDTO dto);

    List<ProductResponseDTO> findAll();

    ProductResponseDTO findById(UUID id);

    ProductResponseDTO update(UUID id, ProductUpdateDTO dto);

    void softDelete(UUID id);

    List<ProductResponseDTO> findByRestaurant(UUID restaurantId);

    //List<ProductResponseDTO> findByCategory(ProductCategory category);

    List<ProductResponseDTO> findAvailable();

    List<ProductResponseDTO> searchAvailable(String query);

    List<ProductResponseDTO> findByCategory(Long CategoryId);


    ProductResponseDTO updateAvailability(
            UUID id,
            boolean available
    );

}
