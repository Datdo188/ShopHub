package com.example.ShopHub.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid Email")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password require at least 6 characters")
    private String password;

    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    @Pattern(regexp = "^(\\+84|0)[0-9]{9}$", message = "Phone number cannot be blank")
    private String phone;
}