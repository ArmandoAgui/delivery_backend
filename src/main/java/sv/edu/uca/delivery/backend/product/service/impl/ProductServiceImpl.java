package sv.edu.uca.delivery.backend.product.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.product.dto.ProductCreateDTO;
import sv.edu.uca.delivery.backend.product.dto.ProductUpdateDTO;
import sv.edu.uca.delivery.backend.product.dto.response.ProductResponseDTO;
import sv.edu.uca.delivery.backend.product.entity.Product;
import sv.edu.uca.delivery.backend.product.exception.ProductNotFoundException;
import sv.edu.uca.delivery.backend.product.mapper.ProductMapper;
import sv.edu.uca.delivery.backend.product.repository.ProductRepository;
import sv.edu.uca.delivery.backend.product.service.ProductService;
import sv.edu.uca.delivery.backend.restaurant.entity.Restaurant;
import sv.edu.uca.delivery.backend.restaurant.exception.RestaurantNotFoundException;
import sv.edu.uca.delivery.backend.restaurant.repository.RestaurantRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public ProductResponseDTO create(ProductCreateDTO dto) {

        Restaurant restaurant = restaurantRepository
                .findByIdAndActiveTrue(dto.getRestaurantId())
                .orElseThrow(RestaurantNotFoundException::new);

        Product product = new Product();

        product.setRestaurant(restaurant);
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());

        // disponible por defecto
        product.setAvailable(true);

        productRepository.save(product);

        return ProductMapper.toDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAll() {

        return productRepository.findAll()
                .stream()
                .map(ProductMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO findById(UUID id) {

        Product product = productRepository
                .findById(id)
                .orElseThrow(ProductNotFoundException::new);

        return ProductMapper.toDTO(product);
    }

    @Override
    @Transactional
    public ProductResponseDTO update(UUID id, ProductUpdateDTO dto) {

        Product product = productRepository
                .findById(id)
                .orElseThrow(ProductNotFoundException::new);

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());

        productRepository.save(product);

        return ProductMapper.toDTO(product);
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {

        Product product = productRepository
                .findById(id)
                .orElseThrow(ProductNotFoundException::new);

        // desactivar disponibilidad
        product.setAvailable(false);

        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findByRestaurant(UUID restaurantId) {

        return productRepository
                .findByRestaurantId(restaurantId)
                .stream()
                .map(ProductMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAvailable() {

        return productRepository
                .findByAvailableTrue()
                .stream()
                .map(ProductMapper::toDTO)
                .toList();
    }
}