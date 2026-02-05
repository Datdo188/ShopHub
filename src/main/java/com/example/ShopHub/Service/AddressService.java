package com.example.ShopHub.Service;

import com.example.ShopHub.Entity.Address;
import com.example.ShopHub.Entity.User;
import com.example.ShopHub.Repository.AddressRepository;
import com.example.ShopHub.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Address createAddress(UUID userId, Address address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        address.setUser(user);

        long addressCount = addressRepository.countByUserId(userId);
        if (addressCount == 0) {
            address.setDefault(true);
        } else if (address.isDefault()) {
            removeDefaultAddress(userId);
        }

        return addressRepository.save(address);
    }

    public List<Address> getAddressesByUser(UUID userId) {
        return addressRepository.findByUserIdAndDeletedFalse(userId);
    }

    public Optional<Address> getAddressById(UUID id) {
        return addressRepository.findById(id);
    }

    public Optional<Address> getDefaultAddress(UUID userId) {
        return addressRepository.findByUserIdAndIsDefaultTrue(userId);
    }

    @Transactional
    public Address updateAddress(UUID id, Address updatedAddress) {
        Address existing = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (updatedAddress.getFullName() != null) {
            existing.setFullName(updatedAddress.getFullName());
        }
        if (updatedAddress.getPhone() != null) {
            existing.setPhone(updatedAddress.getPhone());
        }
        if (updatedAddress.getAddress() != null) {
            existing.setAddress(updatedAddress.getAddress());
        }
        if (updatedAddress.getProvince() != null) {
            existing.setProvince(updatedAddress.getProvince());
        }
        if (updatedAddress.getDistrict() != null) {
            existing.setDistrict(updatedAddress.getDistrict());
        }
        if (updatedAddress.getWard() != null) {
            existing.setWard(updatedAddress.getWard());
        }

        if (updatedAddress.isDefault() && !existing.isDefault()) {
            removeDefaultAddress(existing.getUser().getId());
            existing.setDefault(true);
        }

        return addressRepository.save(existing);
    }

    @Transactional
    public Address setDefaultAddress(UUID id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.isDefault()) {
            removeDefaultAddress(address.getUser().getId());
            address.setDefault(true);
            return addressRepository.save(address);
        }

        return address;
    }

    @Transactional
    public void deleteAddress(UUID id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (address.isDefault()) {
            List<Address> otherAddresses = addressRepository.findByUserIdAndDeletedFalse(
                            address.getUser().getId()
                    ).stream()
                    .filter(a -> !a.getId().equals(id))
                    .toList();

            if (!otherAddresses.isEmpty()) {
                Address newDefault = otherAddresses.get(0);
                newDefault.setDefault(true);
                addressRepository.save(newDefault);
            }
        }

        address.setDeleted(true);
        addressRepository.save(address);
    }

    private void removeDefaultAddress(UUID userId) {
        Optional<Address> defaultAddress = addressRepository.findByUserIdAndIsDefaultTrue(userId);
        defaultAddress.ifPresent(address -> {
            address.setDefault(false);
            addressRepository.save(address);
        });
    }

    public boolean isAddressOwnedByUser(UUID addressId, UUID userId) {
        return addressRepository.findById(addressId)
                .map(address -> address.getUser().getId().equals(userId))
                .orElse(false);
    }
}
