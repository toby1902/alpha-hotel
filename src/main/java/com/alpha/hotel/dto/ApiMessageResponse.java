package com.alpha.hotel.dto;

public class ApiMessageResponse {

    private final String message;

    public ApiMessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
