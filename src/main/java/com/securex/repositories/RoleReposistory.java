package com.securex.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securex.entity.Role;

public interface RoleReposistory extends JpaRepository<Role, UUID> {
	Optional<Role> findByName(String name);

}
