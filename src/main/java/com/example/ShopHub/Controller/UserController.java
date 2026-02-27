package com.example.ShopHub.Controller;

import com.example.ShopHub.DTO.UserDTO;
import com.example.ShopHub.DTO.UserUpdateRequest;
import com.example.ShopHub.Entity.User;
import com.example.ShopHub.Exception.ResourceNotFoundException;
import com.example.ShopHub.Exception.UnauthorizedException;
import com.example.ShopHub.Mapper.UserMapper;
import com.example.ShopHub.Repository.UserRepository;
import com.example.ShopHub.Security.CustomUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        String email = getCurrentUserEmail();
        User user = userDetailsService.loadUserEntityByEmail(email);
        return ResponseEntity.ok(userMapper.toDTO(user));
    }

    /**
     * Update current user profile
     */
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        String email = getCurrentUserEmail();
        User user = userDetailsService.loadUserEntityByEmail(email);

        userMapper.updateEntityFromRequest(user, request);
        User updatedUser = userRepository.save(user);

        return ResponseEntity.ok(userMapper.toDTO(updatedUser));
    }

    /**
     * Get all users (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userRepository.findAll().stream()
                .filter(u -> !u.isDeleted())
                .collect(Collectors.toList());

        List<UserDTO> userDTOs = users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    /**
     * Get user by ID (Admin only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return ResponseEntity.ok(userMapper.toDTO(user));
    }

    /**
     * Update user (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userMapper.updateEntityFromRequest(user, request);
        User updatedUser = userRepository.save(user);

        return ResponseEntity.ok(userMapper.toDTO(updatedUser));
    }

    /**
     * Delete user (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setDeleted(true);
        userRepository.save(user);

        return ResponseEntity.ok().body("User deleted successfully");
    }

    /**
     * Helper method to get current user email
     */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        return authentication.getName();
    }
}