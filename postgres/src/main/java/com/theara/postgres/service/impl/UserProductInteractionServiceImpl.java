package com.theara.postgres.service.impl;

import com.theara.postgres.model.response.UserProductInteraction;
import com.theara.postgres.repository.UserProductInteractionRepository;
import com.theara.postgres.service.UserProductInteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProductInteractionServiceImpl implements UserProductInteractionService {

    @Autowired
    private UserProductInteractionRepository userProductInteractionRepository;

    @Override
    public UserProductInteraction saveUserProductInteraction(UserProductInteraction userProductInteraction) {
        return userProductInteractionRepository.save(userProductInteraction);
    }

    @Override
    public UserProductInteraction findByUserIdAndProductId(Long userId, Long productId) {
        return userProductInteractionRepository
                .findByUser_IdAndProduct_Id(userId, productId); // Custom query method in repository
    }
}

