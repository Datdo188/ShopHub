package com.example.ShopHub.DTO;

import com.example.ShopHub.Enum.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "password cannot be blank")
    private String password;

    @Pattern(regexp = "^(\\+84|0)[0-9]{9}$", message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    private String avatarUrl;

    private Role role = Role.USER;
}