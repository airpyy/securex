package com.securex.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.securex.dtos.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException notFoundException){
		ErrorResponse response = new ErrorResponse(notFoundException.getMessage(),HttpStatus.NOT_FOUND,404);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException illegalArgumentException){
		 ErrorResponse response = new ErrorResponse(illegalArgumentException.getMessage(),HttpStatus.BAD_REQUEST,400);
		 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		 
	}

}
