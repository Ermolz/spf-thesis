package com.example.freelance.mapper.user;

import com.example.freelance.domain.user.User;
import com.example.freelance.dto.user.UserResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    List<UserResponse> toResponseList(List<User> users);
}

