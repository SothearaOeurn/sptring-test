package com.theara.postgres.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "users", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;

    private String username;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String email;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Column(length = 2000)
    private String accessToken;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int expiresIn;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int refreshExpiresIn;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Column(length = 2000)
    private String refreshToken;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String tokenType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer notBeforePolicy;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String sessionState;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String scope;
}