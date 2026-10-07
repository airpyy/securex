package com.securex.implementations;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.securex.config.AppConstent;
import com.securex.dtos.UsersDto;
import com.securex.repositories.RoleReposistory;
import com.securex.services.AuthService;
import com.securex.services.UsersServices;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthServiceImplementation implements AuthService {
	private final UsersServices services;
    private final PasswordEncoder encoder;
	@Override
	public UsersDto registerUsers(UsersDto usersDto) {
		usersDto.setPassword(encoder.encode(usersDto.getPassword()));
		return services.createUsers(usersDto);
	}

}
