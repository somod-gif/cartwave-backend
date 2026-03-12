package com.cartwave.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cartwave.auth.dto.UserDTO;
import com.cartwave.user.entity.User;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface UserMapper {

    UserDTO toUserDTO(User user);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "passwordResetToken", ignore = true)
    @Mapping(target = "passwordResetExpiresAt", ignore = true)
    @Mapping(target = "emailVerificationToken", ignore = true)
    User toUser(UserDTO userDTO);

}
