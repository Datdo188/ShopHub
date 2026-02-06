package com.example.ShopHub.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressCreateRequest {

    @NotBlank(message = "Full name cannot be blank!")
    private String fullName;

    @NotBlank(message = "Phone cannot be blank!")
    @Pattern(regexp = "^(\\+84|0)[0-9]{9}$", message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "Address cannot be blank!")
    private String address;

    @NotBlank(message = "Province cannot be blank!")
    private String province;

    @NotBlank(message = "District cannot be blank!")
    private String district;

    @NotBlank(message = "Ward cannot be blank!")
    private String ward;

    private boolean isDefault = false;
}