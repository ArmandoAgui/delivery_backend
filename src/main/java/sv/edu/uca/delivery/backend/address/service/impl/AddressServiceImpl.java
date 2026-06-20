package sv.edu.uca.delivery.backend.address.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sv.edu.uca.delivery.backend.address.dto.request.CreateAddressRequest;
import sv.edu.uca.delivery.backend.address.dto.response.AddressResponse;
import sv.edu.uca.delivery.backend.address.entity.Address;
import sv.edu.uca.delivery.backend.address.mapper.AddressMapper;
import sv.edu.uca.delivery.backend.address.repository.AddressRepository;
import sv.edu.uca.delivery.backend.address.service.AddressService;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.exception.UserNotFoundException;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    public AddressResponse create(UUID userId, CreateAddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Address address = new Address();
        address.setUser(user);
        address.setLabel(request.getLabel());
        address.setStreetAddress(request.getStreetAddress());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());
        address.setDefaultAddress(request.isDefaultAddress());

        Address saved = addressRepository.save(address);
        return addressMapper.toResponse(saved);
    }

    @Override
    public List<AddressResponse> findByUser(UUID userId) {
        return addressRepository.findByUserId(userId)
                .stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(UUID addressId) {
        addressRepository.deleteById(addressId);
    }
}
