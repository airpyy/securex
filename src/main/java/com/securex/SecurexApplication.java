package com.securex;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.securex.config.AppConstent;
import com.securex.entity.Role;
import com.securex.repositories.RoleReposistory;

@SpringBootApplication
public class SecurexApplication implements CommandLineRunner{
	@Autowired
	private RoleReposistory reposistory;

	public static void main(String[] args) {
		SpringApplication.run(SecurexApplication.class, args);
		
	}

	@Override
	public void run(String... args) throws Exception {
		reposistory.findByName("ROLE_"+AppConstent.ADMIN_ROLE).ifPresentOrElse(role->{},()->{
			Role role = new Role();
			role.setName("ROLE_"+AppConstent.ADMIN_ROLE);
			role.setId(UUID.randomUUID());
	        reposistory.save(role);
			
		});
		reposistory.findByName("ROLE_"+AppConstent.GUEST_ROLE).ifPresentOrElse(role->{},()->{
			Role role = new Role();
			role.setName("ROLE_"+AppConstent.GUEST_ROLE);
			role.setId(UUID.randomUUID());
	        reposistory.save(role);
			
		});
		
	}

}
