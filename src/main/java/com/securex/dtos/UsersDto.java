package com.securex.dtos;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.securex.entity.Provider;
import com.securex.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jdk.jfr.Name;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsersDto {

    private UUID id;
     
    @Email(message = "email mus have @ and domain")
    @NotBlank(message = "it is required")
    private String email;

    private String name;

    private String image;
    
    private String password;

    private Boolean enabled=true;

    private Instant createdAt;

    private Instant updatedAt;

    private Provider provider;

    private Set<RoleDto> roles;
}