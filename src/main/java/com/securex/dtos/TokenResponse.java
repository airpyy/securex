package com.securex.dtos;

public record TokenResponse(

		String accessToken, String refreshToken, long expiresIn, String tokenType, UsersDto users

) {

	public static TokenResponse of(String accessToken, String refreshToken, long expiresIn, UsersDto users) {

		return new TokenResponse(accessToken, refreshToken, expiresIn, "Bearer", users);
	}
}
