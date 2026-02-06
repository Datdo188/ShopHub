package com.example.ShopHub.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private UUID id;
    private String name;
    private String slug;
    private String imageUrl;
    private UUID parentId;
    private String parentName;
    private List<CategoryDTO> children;
    private Integer productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}