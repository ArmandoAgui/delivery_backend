package sv.edu.uca.delivery.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sv.edu.uca.delivery.backend.service.ImageStorageService;
import sv.edu.uca.delivery.backend.dto.ProductCreateDTO;
import sv.edu.uca.delivery.backend.dto.ProductUpdateDTO;
import sv.edu.uca.delivery.backend.dto.ProductResponseDTO;
import sv.edu.uca.delivery.backend.entity.Product;
import sv.edu.uca.delivery.backend.exception.ProductNotFoundException;
import sv.edu.uca.delivery.backend.mapper.ProductMapper;
import sv.edu.uca.delivery.backend.repository.ProductRepository;
import sv.edu.uca.delivery.backend.service.ProductService;
import sv.edu.uca.delivery.backend.repository.PromotionRepository;
import sv.edu.uca.delivery.backend.entity.Restaurant;
import sv.edu.uca.delivery.backend.exception.RestaurantNotFoundException;
import sv.edu.uca.delivery.backend.repository.RestaurantRepository;
import sv.edu.uca.delivery.backend.entity.Category;
import sv.edu.uca.delivery.backend.exception.CategoryNotFoundException;
import sv.edu.uca.delivery.backend.repository.CategoryRepository;

import sv.edu.uca.delivery.backend.entity.Promotion;
import sv.edu.uca.delivery.backend.security.AccessControlService;
import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final RestaurantRepository restaurantRepository;

    private final CategoryRepository categoryRepository;

    private final PromotionRepository promotionRepository;
    private final AccessControlService accessControlService;
    private final ImageStorageService imageStorageService;

    @Override
    @Transactional
    public ProductResponseDTO create(ProductCreateDTO dto) {

        Restaurant restaurant = restaurantRepository
                .findByIdAndActiveTrue(dto.getRestaurantId())
                .orElseThrow(RestaurantNotFoundException::new);
        accessControlService.requireAdminOrRestaurantOwner(restaurant);

        Product product = new Product();

        Category category = categoryRepository
                .findByIdAndActiveTrue(dto.getCategoryId())
                        .orElseThrow(CategoryNotFoundException::new);
        if (!category.getRestaurant().getId().equals(restaurant.getId())) {
            throw new sv.edu.uca.delivery.backend.exception.BusinessException(org.springframework.http.HttpStatus.BAD_REQUEST, "Category does not belong to restaurant");
        }

        product.setRestaurant(restaurant);
        product.setCategory(category);
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());

        // disponible por defecto
        product.setAvailable(true);

        productRepository.save(product);

        return ProductMapper.toDTO(product, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAll() {

        return productRepository.findAll()
                .stream()
                .map(product ->
                        ProductMapper.toDTO(
                                product,
                                getActivePromotion(product)
                        )
                )
                .toList();
    }



    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findByCategory(Long categoryId) {

        return productRepository
                .findByCategoryId(categoryId)
                .stream()
                .map(product ->
                        ProductMapper.toDTO(
                                product,
                                getActivePromotion(product)
                        )
                )
                .toList();
    }


    @Override
    @Transactional
    public ProductResponseDTO updateAvailability(
            UUID id,
            boolean available
    ){
        Product product = productRepository
                .findById(id)
                .orElseThrow(ProductNotFoundException::new);
        accessControlService.requireAdminOrRestaurantOwner(product.getRestaurant());

        product.setAvailable(available);
        productRepository.save(product);
        return ProductMapper.toDTO(product, null);
    }



    private Promotion getActivePromotion(Product product) {

        return promotionRepository
                .findFirstByRestaurant_IdAndActiveTrue(
                        product.getRestaurant().getId()
                )
                .filter(p -> {
                    LocalDate today = LocalDate.now();

                    return !today.isBefore(p.getStartDate())
                            && !today.isAfter(p.getEndDate());
                })
                .orElse(null);
    }






    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO findById(UUID id) {

        Product product = productRepository
                .findById(id)
                .orElseThrow(ProductNotFoundException::new);
        accessControlService.requireAdminOrRestaurantOwner(product.getRestaurant());

        return ProductMapper.toDTO(
                product,
                getActivePromotion(product)
        );
    }

    @Override
    @Transactional
    public ProductResponseDTO update(UUID id, ProductUpdateDTO dto) {

        Product product = productRepository
                .findById(id)
                .orElseThrow(ProductNotFoundException::new);
        accessControlService.requireAdminOrRestaurantOwner(product.getRestaurant());

        Category category = categoryRepository
                .findByIdAndActiveTrue(dto.getCategoryId())
                        .orElseThrow(CategoryNotFoundException::new);
        if (!category.getRestaurant().getId().equals(product.getRestaurant().getId())) {
            throw new sv.edu.uca.delivery.backend.exception.BusinessException(org.springframework.http.HttpStatus.BAD_REQUEST, "Category does not belong to restaurant");
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(category);
        product.setAvailable(dto.isAvailable());

        productRepository.save(product);

        return ProductMapper.toDTO(product, null);
    }

    @Override
    @Transactional
    public ProductResponseDTO uploadImage(UUID id, MultipartFile file) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(ProductNotFoundException::new);
        accessControlService.requireAdminOrRestaurantOwner(product.getRestaurant());

        product.setImageUrl(imageStorageService.storeProductImage(product.getId(), file, product.getImageUrl()));
        productRepository.save(product);
        return ProductMapper.toDTO(product, getActivePromotion(product));
    }

    @Override
    @Transactional
    public void deleteImage(UUID id) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(ProductNotFoundException::new);
        accessControlService.requireAdminOrRestaurantOwner(product.getRestaurant());

        imageStorageService.delete(product.getImageUrl());
        product.setImageUrl(null);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {

        Product product = productRepository
                .findById(id)
                .orElseThrow(ProductNotFoundException::new);
        accessControlService.requireAdminOrRestaurantOwner(product.getRestaurant());

        // desactivar disponibilidad
        product.setAvailable(false);
        imageStorageService.delete(product.getImageUrl());
        product.setImageUrl(null);

        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findByRestaurant(UUID restaurantId) {

        return productRepository
                .findByRestaurantId(restaurantId)
                .stream()
                .map(product ->
                        ProductMapper.toDTO(
                                product,
                                getActivePromotion(product)
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAvailable() {

        return productRepository
                .findByAvailableTrue()
                .stream()
                .map(product -> {

                    Promotion promotion =
                            promotionRepository
                                    .findFirstByRestaurant_IdAndActiveTrue(
                                            product.getRestaurant().getId()
                                    )
                                    .filter(p -> {
                                        LocalDate today = LocalDate.now();

                                        return !today.isBefore(p.getStartDate())
                                                && !today.isAfter(p.getEndDate());
                                    })
                                    .orElse(null);

                    return ProductMapper.toDTO(
                            product,
                            promotion
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> searchAvailable(String query) {
        if (query == null || query.isBlank()) {
            return findAvailable();
        }
        return productRepository.searchAvailable(query.trim())
                .stream()
                .map(product -> ProductMapper.toDTO(product, getActivePromotion(product)))
                .toList();
    }
}
