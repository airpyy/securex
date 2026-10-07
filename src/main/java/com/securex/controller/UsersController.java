package com.securex.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.securex.dtos.UsersDto;
import com.securex.services.UsersServices;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/users")
public class UsersController {

	private final UsersServices usersServices;
	
	//Create User

    @PostMapping
	public ResponseEntity<UsersDto> createdUser(@Valid @RequestBody UsersDto userdto){
			 return ResponseEntity.status(HttpStatus.CREATED).body(usersServices.createUsers(userdto));
		}
    
    //Get All Users
    @GetMapping
    public ResponseEntity<Iterable<UsersDto>> getAllUsers(){
    	return  ResponseEntity.ok(usersServices.getAllUsers());
    	
    }
    
    //Get a Users With Email
    @GetMapping("/email/{email}")
    public ResponseEntity<UsersDto> getUsersByEmail(@PathVariable String email){
    	return ResponseEntity.ok(usersServices.getUsersByEmail(email));
    }
    
    //delete users with the id
    
    @DeleteMapping("/{userid}")
    public void deleteUserByid(@PathVariable String userid) {
    	usersServices.deleteUsers(userid);
    }
    
    //update users
    @PutMapping("/{userid}")
    public ResponseEntity<UsersDto> updateUserByid(@RequestBody UsersDto dto,@PathVariable String userid){
    	return ResponseEntity.ok(usersServices.updateUser(dto, userid));
    }
    
    //get users by id
    @GetMapping("/{userid}")
    public ResponseEntity<UsersDto> getUserById(@PathVariable String userid){
    	return ResponseEntity.ok(usersServices.getUserById(userid));
    	
    }
    
    
}
