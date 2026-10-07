package com.securex.services;

import com.securex.dtos.UsersDto;

public interface UsersServices {

	UsersDto createUsers(UsersDto usersDto);
	
	UsersDto getUsersByEmail(String email);
	
	UsersDto updateUser(UsersDto usersDto , String userid);
	
	void deleteUsers(String userid);
	
	UsersDto getUserById(String userid);
	
	Iterable<UsersDto>getAllUsers();
	
}
