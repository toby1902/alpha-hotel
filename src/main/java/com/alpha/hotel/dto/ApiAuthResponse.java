package com.alpha.hotel.dto;

public class ApiAuthResponse {

    private final String token;
    private final String type = "Bearer";
    private final String email;
    private final String role;

    public ApiAuthResponse(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
