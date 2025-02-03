package com.theara.postgres.model.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String email;
    private String password;
}