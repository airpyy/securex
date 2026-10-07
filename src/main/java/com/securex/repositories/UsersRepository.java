package com.securex.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.securex.entity.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, UUID>  {
	Optional<Users> findByemail(String email);
	boolean existsByEmail(String email);

}
