package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.user.UserCreateRequest;
import com.antipanel.backend.dto.user.UserProfileUpdateRequest;
import com.antipanel.backend.dto.user.UserResponse;
import com.antipanel.backend.dto.user.UserSummary;
import com.antipanel.backend.dto.user.UserUpdateRequest;
import com.antipanel.backend.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for User entity.
 * Handles conversion between User entity and DTOs.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper extends BaseMapper<User, UserResponse, UserCreateRequest, UserUpdateRequest, UserSummary> {

    /**
     * Convert User entity to UserResponse DTO.
     * Password hash is automatically excluded (not in DTO).
     */
    @Override
    UserResponse toResponse(User user);

    /**
     * Convert UserCreateRequest to User entity.
     * Note: Password hashing must be done in service layer.
     */
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)  // Password must be hashed by service
    @Mapping(target = "balance", ignore = true)        // Set default in service
    @Mapping(target = "isBanned", ignore = true)       // Default: false
    @Mapping(target = "bannedReason", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "loginCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserCreateRequest createRequest);

    /**
     * Update User entity from UserUpdateRequest.
     * Null values are ignored (supports partial updates).
     * Note: Password hashing must be done in service layer.
     */
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)  // Password must be hashed by service
    @Mapping(target = "balance", ignore = true)        // Not updatable via DTO
    @Mapping(target = "role", ignore = true)           // SECURITY: Prevent privilege escalation
    @Mapping(target = "isBanned", ignore = true)       // SECURITY: Prevent self-unban
    @Mapping(target = "bannedReason", ignore = true)   // SECURITY: Prevent ban reason modification
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "loginCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UserUpdateRequest updateRequest, @MappingTarget User user);

    /**
     * Update User entity from UserProfileUpdateRequest (user self-update).
     * SECURITY: This method is for user self-updates only.
     * Sensitive fields (role, isBanned, bannedReason) are not present in the DTO.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "isBanned", ignore = true)
    @Mapping(target = "bannedReason", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "loginCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateProfileFromDto(UserProfileUpdateRequest profileRequest, @MappingTarget User user);

    /**
     * Convert User entity to UserSummary DTO.
     */
    @Override
    UserSummary toSummary(User user);

    /**
     * Convert list of Users to list of UserResponse DTOs.
     */
    @Override
    List<UserResponse> toResponseList(List<User> users);

    /**
     * Convert list of Users to list of UserSummary DTOs.
     */
    @Override
    List<UserSummary> toSummaryList(List<User> users);
}
