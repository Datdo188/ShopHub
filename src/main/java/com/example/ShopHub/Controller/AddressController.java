package com.example.ShopHub.Controller;

import com.example.ShopHub.DTO.AddressCreateRequest;
import com.example.ShopHub.DTO.AddressDTO;
import com.example.ShopHub.Entity.Address;
import com.example.ShopHub.Exception.ResourceNotFoundException;
import com.example.ShopHub.Exception.UnauthorizedException;
import com.example.ShopHub.Security.CustomUserDetailsService;
import com.example.ShopHub.Service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "*")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Get all addresses of current user
     */
    @GetMapping
    public ResponseEntity<List<AddressDTO>> getMyAddresses() {
        UUID userId = getCurrentUserId();
        List<Address> addresses = addressService.getAddressesByUser(userId);

        List<AddressDTO> addressDTOs = addresses.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(addressDTOs);
    }

    /**
     * Get address by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();

        Address address = addressService.getAddressById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));

        // Check if address belongs to current user
        if (!address.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only view your own addresses");
        }

        return ResponseEntity.ok(toDTO(address));
    }

    /**
     * Get default address
     */
    @GetMapping("/default")
    public ResponseEntity<AddressDTO> getDefaultAddress() {
        UUID userId = getCurrentUserId();

        Address address = addressService.getDefaultAddress(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Default address not found"));

        return ResponseEntity.ok(toDTO(address));
    }

    /**
     * Create new address
     */
    @PostMapping
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressCreateRequest request) {
        UUID userId = getCurrentUserId();

        Address address = new Address();
        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setAddress(request.getAddress());
        address.setProvince(request.getProvince());
        address.setDistrict(request.getDistrict());
        address.setWard(request.getWard());
        address.setDefault(request.isDefault());

        Address createdAddress = addressService.createAddress(userId, address);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdAddress));
    }

    /**
     * Update address
     */
    @PutMapping("/{id}")
    public ResponseEntity<AddressDTO> updateAddress(
            @PathVariable UUID id,
            @Valid @RequestBody AddressCreateRequest request
    ) {
        UUID userId = getCurrentUserId();

        // Check if address belongs to current user
        if (!addressService.isAddressOwnedByUser(id, userId)) {
            throw new UnauthorizedException("You can only update your own addresses");
        }

        Address updated = new Address();
        updated.setFullName(request.getFullName());
        updated.setPhone(request.getPhone());
        updated.setAddress(request.getAddress());
        updated.setProvince(request.getProvince());
        updated.setDistrict(request.getDistrict());
        updated.setWard(request.getWard());
        updated.setDefault(request.isDefault());

        Address updatedAddress = addressService.updateAddress(id, updated);

        return ResponseEntity.ok(toDTO(updatedAddress));
    }

    /**
     * Set address as default
     */
    @PatchMapping("/{id}/set-default")
    public ResponseEntity<AddressDTO> setDefaultAddress(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();

        // Check if address belongs to current user
        if (!addressService.isAddressOwnedByUser(id, userId)) {
            throw new UnauthorizedException("You can only update your own addresses");
        }

        Address address = addressService.setDefaultAddress(id);

        return ResponseEntity.ok(toDTO(address));
    }

    /**
     * Delete address
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();

        // Check if address belongs to current user
        if (!addressService.isAddressOwnedByUser(id, userId)) {
            throw new UnauthorizedException("You can only delete your own addresses");
        }

        addressService.deleteAddress(id);

        return ResponseEntity.ok().body("Address deleted successfully");
    }

    /**
     * Helper method to get current user ID
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        String email = authentication.getName();
        return userDetailsService.loadUserEntityByEmail(email).getId();
    }

    /**
     * Helper method to convert Address to DTO
     */
    private AddressDTO toDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setUserId(address.getUser().getId());
        dto.setFullName(address.getFullName());
        dto.setPhone(address.getPhone());
        dto.setAddress(address.getAddress());
        dto.setProvince(address.getProvince());
        dto.setDistrict(address.getDistrict());
        dto.setWard(address.getWard());
        dto.setDefault(address.isDefault());
        dto.setCreatedAt(address.getCreatedAt());
        dto.setUpdatedAt(address.getUpdatedAt());
        return dto;
    }
}