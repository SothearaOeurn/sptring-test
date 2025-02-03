package com.theara.postgres.service;


import com.theara.postgres.model.response.UserProductInteraction;

public interface UserProductInteractionService {

    // Method to save a UserProductInteraction
    UserProductInteraction saveUserProductInteraction(UserProductInteraction userProductInteraction);

    // You can define additional methods to find interactions, e.g., by user or product
    // Example: Find interaction by user and product
    UserProductInteraction findByUserIdAndProductId(Long userId, Long productId);
}

