package com.theara.postgres.repository;

import com.theara.postgres.model.response.UserProductInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProductInteractionRepository extends JpaRepository<UserProductInteraction, Long> {

    // Custom query to find interaction by user and product
    UserProductInteraction findByUser_IdAndProduct_Id(Long userId, Long productId);
}

