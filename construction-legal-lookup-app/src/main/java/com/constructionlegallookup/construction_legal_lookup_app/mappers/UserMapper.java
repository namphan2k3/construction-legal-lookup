package com.constructionlegallookup.construction_legal_lookup_app.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.admin.AdminUserDto;
import com.constructionlegallookup.construction_legal_lookup_app.dto.responses.user.UserResponse;
import com.constructionlegallookup.construction_legal_lookup_app.entities.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().name() : null)")
    UserResponse toUserResponse(User user);
    
    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().name() : null)")
    AdminUserDto toAdminUserDto(User user);
}
