package com.acemouty.advicestore.repositories;

import com.acemouty.advicestore.exceptions.DatabaseAccessException;
import com.acemouty.advicestore.exceptions.InvalidProductDataException;
import com.acemouty.advicestore.exceptions.ProductAlreadyExistsException;
import com.acemouty.advicestore.exceptions.ProductNotFoundException;
import com.acemouty.advicestore.models.Product;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ProductRepository {

    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private Long idCounter = 1L;

    public ProductRepository() {
        System.out.println("Starting ProductRepository initialization...");
        try {
            System.out.println("About to initialize products...");
            initializeZeldaProducts();
            System.out.println("Products initialized successfully");
        } catch (Exception e) {
            System.err.println("ERROR in ProductRepository constructor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeZeldaProducts() {
        // Initialize with some Zelda-themed products
        products.put(idCounter, new Product(idCounter++, "Master Sword", "The legendary blade that seals the darkness", new BigDecimal("999.99"), "Weapons", 1));
        products.put(idCounter,new Product(idCounter++, "Hylian Shield", "An indestructible shield blessed by the goddess", new BigDecimal("750.00"), "Shields", 3));
        products.put(idCounter,new Product(idCounter++, "Bow of Light", "A sacred bow capable of banishing evil", new BigDecimal("650.00"), "Weapons", 2));
        products.put(idCounter,new Product(idCounter++, "Zora Armor", "Allows swimming up waterfalls", new BigDecimal("400.00"), "Armor", 5));
        products.put(idCounter,new Product(idCounter++, "Korok Seed", "A gift from the forest spirits", new BigDecimal("10.00"), "Collectibles", 900));
        products.put(idCounter,new Product(idCounter++, "Bomb Flower", "Explosive plant found in caves", new BigDecimal("25.00"), "Consumables", 50));
        products.put(idCounter,new Product(idCounter++, "Hearty Radish", "Restores all hearts and adds temporary ones", new BigDecimal("35.00"), "Consumables", 20));
    }

    public List<Product> findAll() {
        // Simulate occasional database issues
        if (Math.random() < 0.3) { // 30% chance
            throw new DatabaseAccessException("Connection to Hyrule Database failed");
        }
        return new ArrayList<>(products.values());
    }

    public Optional<Product> findById(Long id) {
        // Simulate database connectivity issues for specific IDs
        if (id != null && id == 666L) {
            throw new DatabaseAccessException("Cursed product ID caused database corruption");
        }

        return Optional.ofNullable(products.get(id));
    }

    public Product save(Product product) {
        if (product.getName() != null && product.getName().toLowerCase().contains("ganondorf")) {
            throw new InvalidProductDataException("Products containing 'Ganondorf' are forbidden in Hyrule");
        }

        if (product.getId() == null) {
            // New product
            if (findByName(product.getName()).isPresent()) {
                throw new ProductAlreadyExistsException("Product with name '" + product.getName() + "' already exists");
            }
            product.setId(idCounter++);
            product.setCreatedAt(LocalDateTime.now());
        } else {
            // Update existing product
            if (!products.containsKey(product.getId())) {
                throw new ProductNotFoundException("Product with ID " + product.getId() + " not found for update");
            }
            product.setUpdatedAt(LocalDateTime.now());
        }

        // Simulate stock validation
        if (product.getStock() != null && product.getStock() < 0) {
            throw new InvalidProductDataException("Stock cannot be negative");
        }

        // Simulate price validation
        if (product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidProductDataException("Price cannot be negative");
        }

        products.put(product.getId(), product);
        return product;
    }

    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        if (!products.containsKey(id)) {
            throw new ProductNotFoundException("Product with ID " + id + " not found for deletion");
        }

        // Simulate protection for legendary items
        Product product = products.get(id);
        if (product.getName().toLowerCase().contains("master sword") ||
                product.getName().toLowerCase().contains("triforce")) {
            throw new InvalidProductDataException("Legendary items cannot be deleted from Hyrule's inventory");
        }

        products.remove(id);
    }

    public List<Product> findByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be null or empty");
        }

        // Simulate database timeout for certain categories
        if (category.toLowerCase().equals("cursed")) {
            throw new DatabaseAccessException("Access to cursed items category is forbidden");
        }

        return products.values().stream()
                .filter(product -> product.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null) {
            throw new IllegalArgumentException("Price range cannot contain null values");
        }

        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }

        // Simulate expensive query timeout
        if (maxPrice.compareTo(new BigDecimal("10000")) > 0) {
            throw new DatabaseAccessException("Price range too large, query timed out");
        }

        return products.values().stream()
                .filter(product -> product.getPrice().compareTo(minPrice) >= 0 &&
                        product.getPrice().compareTo(maxPrice) <= 0)
                .collect(Collectors.toList());
    }

    public List<Product> findByNameContaining(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Search name cannot be null or empty");
        }

        // Simulate search restrictions
        if (name.toLowerCase().contains("triforce")) {
            throw new DatabaseAccessException("Search for Triforce items requires special authorization");
        }

        return products.values().stream()
                .filter(product -> product.getName().toLowerCase()
                        .contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    private Optional<Product> findByName(String name) {
        return products.values().stream()
                .filter(product -> product.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}
