package com.theara.postgres.controller;

import com.theara.postgres.config.JwtUtil;
import com.theara.postgres.model.response.Product;
import com.theara.postgres.model.response.User;
import com.theara.postgres.model.response.UserProductInteraction;
import com.theara.postgres.service.ProductService;
import com.theara.postgres.service.UserProductInteractionService;
import com.theara.postgres.service.UserService;
import com.theara.postgres.service.impl.KeycloakServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private UserProductInteractionService userProductInteractionService;  // Using the service interface

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private KeycloakServiceImpl keycloakServiceImpl;
    @Autowired
    private JwtUtil jwtUtil;

    // Create a new product
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product, @RequestHeader("Authorization") String authorizationHeader) {
        // Step 1: Create the product
        try {
            String token = authorizationHeader.substring(7); // Remove "Bearer " prefix
            String userId = keycloakServiceImpl.getUserIdFromToken(token);
            Optional<User> userOptional = userService.getUserByUserId(userId);
            UserProductInteraction userProductInteraction = new UserProductInteraction();
            Product createdProduct = productService.createProduct(product);
            if (userOptional.isPresent()) {
                userProductInteraction.setUser(userOptional.get());  // Set the user
            }
            userProductInteraction.setProduct(createdProduct);  // Set the product
            userProductInteraction.setCreatedAt(LocalDateTime.now());  // Set the created timestamp
            userProductInteractionService.saveUserProductInteraction(userProductInteraction);
            // Step 5: Return the created product
            return ResponseEntity.ok(createdProduct);
        } catch (HttpClientErrorException e) {
            // Log the exception (you can customize this further)
            throw new RuntimeException("Error updating user", e);
        }
    }

    // Retrieve all products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    // Retrieve a product by ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    // Update a product
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Product updatedProduct = productService.updateProduct(id, product);
        return ResponseEntity.ok(updatedProduct);
    }

    // Delete a product
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}

