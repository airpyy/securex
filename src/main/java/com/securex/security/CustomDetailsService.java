package com.securex.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.securex.exceptions.ResourceNotFoundException;
import com.securex.repositories.UsersRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class CustomDetailsService implements UserDetailsService{
	private final UsersRepository repository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return repository.findByemail(username).orElseThrow(()->new ResourceNotFoundException("Please Check Your Credentials"));
	}
	

}
