package com.securex.services;

import com.securex.dtos.UsersDto;

public interface AuthService {
    UsersDto registerUsers(UsersDto usersDto);
}
