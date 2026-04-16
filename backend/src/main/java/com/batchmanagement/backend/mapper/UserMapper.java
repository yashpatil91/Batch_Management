package com.batchmanagement.backend.mapper;

import com.batchmanagement.backend.dto.common.UserResponse;
import com.batchmanagement.backend.entity.User;

public final class UserMapper {

    private UserMapper() {}

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}