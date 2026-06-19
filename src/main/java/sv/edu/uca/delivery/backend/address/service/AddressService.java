package sv.edu.uca.delivery.backend.address.service;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.address.dto.AddressRequest;
import sv.edu.uca.delivery.backend.address.dto.AddressResponse;
import sv.edu.uca.delivery.backend.address.entity.Address;
import sv.edu.uca.delivery.backend.address.repository.AddressRepository;
import sv.edu.uca.delivery.backend.common.exception.BusinessException;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public List<AddressResponse> myAddresses() {
        return addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(currentUserId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AddressResponse create(AddressRequest request) {
        User user = userRepository.findByIdAndActiveTrue(currentUserId())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Authenticated user does not exist"));
        Address address = new Address();
        address.setUser(user);
        apply(address, request);
        if (request.defaultAddress()) {
            clearDefaults(user.getId());
        }
        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(UUID id, AddressRequest request) {
        Address address = addressRepository.findByIdAndUserId(id, currentUserId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Address not found"));
        apply(address, request);
        if (request.defaultAddress()) {
            clearDefaults(address.getUser().getId());
            address.setDefaultAddress(true);
        }
        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public void delete(UUID id) {
        Address address = addressRepository.findByIdAndUserId(id, currentUserId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Address not found"));
        addressRepository.delete(address);
    }

    private void apply(Address address, AddressRequest request) {
        address.setLabel(request.label());
        address.setStreetAddress(request.streetAddress());
        address.setCity(request.city());
        address.setState(request.state());
        address.setDefaultAddress(request.defaultAddress());
        address.setLocation(toLocation(request.latitude(), request.longitude()));
    }

    private void clearDefaults(UUID userId) {
        addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(userId)
                .forEach(address -> {
                    address.setDefaultAddress(false);
                    addressRepository.save(address);
                });
    }

    private AddressResponse toResponse(Address address) {
        Point location = address.getLocation();
        return new AddressResponse(
                address.getId(),
                address.getLabel(),
                address.getStreetAddress(),
                address.getCity(),
                address.getState(),
                location == null ? null : location.getY(),
                location == null ? null : location.getX(),
                address.isDefaultAddress()
        );
    }

    private Point toLocation(double latitude, double longitude) {
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);
        return point;
    }

    private UUID currentUserId() {
        return authenticatedUserProvider.getCurrentUserId();
    }
}
