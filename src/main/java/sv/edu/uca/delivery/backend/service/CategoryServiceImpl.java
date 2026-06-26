package sv.edu.uca.delivery.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.dto.CategoryCreateDTO;
import sv.edu.uca.delivery.backend.dto.CategoryUpdateDTO;
import sv.edu.uca.delivery.backend.dto.CategoryResponseDTO;
import sv.edu.uca.delivery.backend.entity.Category;
import sv.edu.uca.delivery.backend.exception.CategoryAlreadyExistsException;
import sv.edu.uca.delivery.backend.exception.CategoryNotFoundException;
import sv.edu.uca.delivery.backend.mapper.CategoryMapper;
import sv.edu.uca.delivery.backend.repository.CategoryRepository;
import sv.edu.uca.delivery.backend.service.CategoryService;
import sv.edu.uca.delivery.backend.entity.Restaurant;
import sv.edu.uca.delivery.backend.exception.RestaurantNotFoundException;
import sv.edu.uca.delivery.backend.repository.RestaurantRepository;
import sv.edu.uca.delivery.backend.security.AccessControlService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final AccessControlService accessControlService;

    @Override
    @Transactional
    public CategoryResponseDTO create(CategoryCreateDTO dto) {

        Restaurant restaurant = restaurantRepository
                .findByIdAndActiveTrue(dto.getRestaurantId())
                .orElseThrow(RestaurantNotFoundException::new);
        accessControlService.requireAdminOrRestaurantOwner(restaurant);

        if (categoryRepository.existsByRestaurant_IdAndNameIgnoreCase(
                dto.getRestaurantId(),
                dto.getName()
        )) {
            throw new CategoryAlreadyExistsException();
        }

        Category category = new Category();

        category.setRestaurant(restaurant);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        categoryRepository.save(category);

        return CategoryMapper.toDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAll() {

        return categoryRepository.findByActiveTrue()
                .stream()
                .map(CategoryMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(Long id) {

        Category category = categoryRepository
                .findByIdAndActiveTrue(id)
                .orElseThrow(CategoryNotFoundException::new);
        accessControlService.requireAdminOrRestaurantOwner(category.getRestaurant());

        return CategoryMapper.toDTO(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO update(
            Long id,
            CategoryUpdateDTO dto
    ) {

        Category category = categoryRepository
                .findByIdAndActiveTrue(id)
                .orElseThrow(CategoryNotFoundException::new);
        accessControlService.requireAdminOrRestaurantOwner(category.getRestaurant());

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        categoryRepository.save(category);

        return CategoryMapper.toDTO(category);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {

        Category category = categoryRepository
                .findByIdAndActiveTrue(id)
                .orElseThrow(CategoryNotFoundException::new);
        accessControlService.requireAdminOrRestaurantOwner(category.getRestaurant());

        category.setActive(false);

        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findByRestaurant(
            UUID restaurantId
    ) {

        return categoryRepository
                .findByRestaurant_IdAndActiveTrue(restaurantId)
                .stream()
                .map(CategoryMapper::toDTO)
                .toList();
    }
}
