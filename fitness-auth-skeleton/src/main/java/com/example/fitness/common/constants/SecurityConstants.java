package com.example.fitness.common.constants;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_ROLE_CODE = "roleCode";
}
