package com.example.ShopHub.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private UUID id;
    private UUID userId;
    private String fullName;
    private String phone;
    private String address;
    private String province;
    private String district;
    private String ward;
    private boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    //helper method
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null) {
            sb.append(address);
        }
        if (ward != null) {
            sb.append(", ").append(ward);
        }
        if (district != null) {
            sb.append(", ").append(district);
        }
        if (province != null) {
            sb.append(", ").append(province);
        }
        return sb.toString();
    }
}