package com.company.approval.auth.dto;

public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private CurrentUserResponse user;

    public LoginResponse(String accessToken, String tokenType, CurrentUserResponse user) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public CurrentUserResponse getUser() {
        return user;
    }
}

