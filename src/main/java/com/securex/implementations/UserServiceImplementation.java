package com.securex.implementations;

import java.time.Instant;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.securex.config.AppConstent;
import com.securex.dtos.UsersDto;
import com.securex.entity.Provider;
import com.securex.entity.Role;
import com.securex.entity.Users;
import com.securex.exceptions.ResourceNotFoundException;
import com.securex.helper.UserHelper;
import com.securex.repositories.RoleReposistory;
import com.securex.repositories.UsersRepository;
import com.securex.services.UsersServices;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service

public class UserServiceImplementation implements UsersServices {
	private final UsersRepository repository;
	private final ModelMapper mapper;
	private final RoleReposistory reposistory;
	@Override
	@Transactional
	public UsersDto createUsers(UsersDto usersDto) {
		if (usersDto.getEmail() == null || usersDto.getEmail().isBlank()) {
			throw new IllegalArgumentException("Email is Required Please Enter Email");
		}
		if (repository.existsByEmail(usersDto.getEmail())) {
			throw new IllegalArgumentException("Email already exists");
		}
		Users users = mapper.map(usersDto, Users.class);
		users.setProvider(usersDto.getProvider() == null ? usersDto.getProvider() : Provider.LOCAL);
		Role role=reposistory.findByName("ROLE_"+AppConstent.GUEST_ROLE).orElse(null);
		users.getRoles().add(role);
		Users savedusers = repository.save(users);
		
		return mapper.map(savedusers, UsersDto.class);
	}

	@Override
	public UsersDto getUsersByEmail(String email) {
		Users users = repository.findByemail(email)
				.orElseThrow(() -> new ResourceNotFoundException("email doesnot exists"));
		return mapper.map(users, UsersDto.class);
	}

	@Override
	public UsersDto updateUser(UsersDto usersDto, String userid) {
		UUID uID = UserHelper.parseUuid(userid);
		Users existinguser = repository.findById(uID)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with this userid"));
		if (usersDto.getName() != null)
			existinguser.setName(usersDto.getName());
		if (usersDto.getImage() != null)
			existinguser.setImage(usersDto.getImage());
		if (usersDto.getProvider() != null)
			existinguser.setProvider(usersDto.getProvider());
		if (usersDto.getPassword() != null)
			existinguser.setPassword(usersDto.getPassword());
		Users users = repository.save(existinguser);
		existinguser.setUpdatedat(Instant.now());
		 return mapper.map(users, UsersDto.class);

	}

	@Override
	public void deleteUsers(String userid) {
		UUID uId = UserHelper.parseUuid(userid);
		Users user = repository.findById(uId).orElseThrow(() -> new ResourceNotFoundException("User not Found"));
		//user.getRoles().clear();
		repository.delete(user);
	}

	@Override
	public UsersDto getUserById(String userid) {
		Users users = repository.findById(UserHelper.parseUuid(userid))
				.orElseThrow(() -> new ResourceNotFoundException("User not Found"));
		return mapper.map(users, UsersDto.class);
	}

	@Override
	public Iterable<UsersDto> getAllUsers() {
		return repository.findAll().stream().map(user -> mapper.map(user, UsersDto.class)).toList();
	}
}
