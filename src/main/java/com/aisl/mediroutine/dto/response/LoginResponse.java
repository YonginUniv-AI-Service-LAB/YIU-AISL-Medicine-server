package com.aisl.mediroutine.dto.response;

import com.aisl.mediroutine.entity.User;
import lombok.Getter;

@Getter
public class LoginResponse {

    private final UserResponse user;

    private LoginResponse(UserResponse user) {
        this.user = user;
    }

    public static LoginResponse from(User user) {
        return new LoginResponse(UserResponse.from(user));
    }
}