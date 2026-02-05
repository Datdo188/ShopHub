package com.example.ShopHub.Repository;

import com.example.ShopHub.Entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByUserId(UUID userId);

    Optional<Address> findByUserIdAndIsDefaultTrue(UUID userId);

    List<Address> findByUserIdAndDeletedFalse(UUID userId);

    long countByUserId(UUID userId);
}