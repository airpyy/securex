package com.securex.controller;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.securex.dtos.LoginRequest;
import com.securex.dtos.RefreshTokenRequest;
import com.securex.dtos.TokenResponse;
import com.securex.dtos.UsersDto;
import com.securex.entity.Provider;
import com.securex.entity.RefreshToken;
import com.securex.entity.Users;
import com.securex.repositories.RefreshTokenRepos;
import com.securex.repositories.UsersRepository;
import com.securex.security.CookieService;
import com.securex.services.AuthService;
import com.securex.services.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService service;
	private final AuthenticationManager authenticationManager;
	private final UsersRepository repository;
	private final RefreshTokenRepos refreshTokenRepos;
	private final JwtService jwtService;
	private final ModelMapper mapper;
	private final CookieService cookieService;

	@PostMapping("/register")
	public ResponseEntity<UsersDto> registerUser(@RequestBody UsersDto user) {
		System.out.print("register hited by the controller");
		user.setProvider(Provider.LOCAL);
		return ResponseEntity.status(HttpStatus.CREATED).body(service.registerUsers(user));
	}

	@PostMapping("/login")
	public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest,
			HttpServletResponse httpServletResponse) {
		Authentication authentication = authenticate(loginRequest);
		Users user = repository.findByemail(loginRequest.email())
				.orElseThrow(() -> new BadCredentialsException("Invalid username and password"));
		if (!user.isEnable()) {
			throw new DisabledException("User is not active");
		}
		String jti = UUID.randomUUID().toString();
		var refreshTokenObj = RefreshToken.builder().jti(jti).users(user).createdAt(Instant.now())
				.expiresAt(Instant.now().plusSeconds(jwtService.getRefreshtokensec())).revoked(false).build();

		refreshTokenRepos.save(refreshTokenObj);
		String accessToken = jwtService.genrateAcessTokens(user);
		String refreshToken = jwtService.generateRefreshToken(user, refreshTokenObj.getJti());
		cookieService.attachRefreshCookie(httpServletResponse, refreshToken, (int) jwtService.getRefreshtokensec());
		TokenResponse response = TokenResponse.of(accessToken, refreshToken, jwtService.getAccessTokenSec(),
				mapper.map(user, UsersDto.class));
		return ResponseEntity.ok(response);
	}

	// access token and refresh token to renew krega
	@PostMapping("/refresh")
	public ResponseEntity<TokenResponse> refreshToken(@RequestBody(required = false) RefreshTokenRequest body,
			HttpServletResponse response, HttpServletRequest request) {
		String refreshtoken = readRefreshTokenFromRequest(body, request)
				.orElseThrow(() -> new BadCredentialsException("Invalid Refresh Token"));

		if (!jwtService.isRefreshToken(refreshtoken)) {
			throw new BadCredentialsException("Invalid Refresh Token Type");
		}
		String jti = jwtService.getJti(refreshtoken);
		UUID userid = jwtService.getUserID(refreshtoken);
		var storedrefreshtoken = refreshTokenRepos.findByJti(jti)
				.orElseThrow(() -> new BadCredentialsException("Invalid token type"));
		if (storedrefreshtoken.isRevoked()) {
			throw new BadCredentialsException("Refresh Token Is Revoked");
		}
		if (storedrefreshtoken.getExpiresAt().isBefore(Instant.now())) {
			throw new BadCredentialsException("Refresh token is expired");
		}
		if (!storedrefreshtoken.getUsers().getId().equals(userid)) {
			throw new BadCredentialsException("Refresh Token doesnot Belong to this user");
		}
		// rotating the refresh token for making it more secure
		// for doing that we have to make the old refresh token revoked then
		// Generate the new refresh token
		storedrefreshtoken.setRevoked(true);
		String newJti = UUID.randomUUID().toString();
		storedrefreshtoken.setReplacedByToken(newJti);
		refreshTokenRepos.save(storedrefreshtoken);
		var newRefreshTokenobj = RefreshToken.builder().jti(newJti).users(storedrefreshtoken.getUsers())
				.createdAt(Instant.now()).expiresAt(Instant.now().plusSeconds(jwtService.getRefreshtokensec()))
				.revoked(false).build();
		refreshTokenRepos.save(newRefreshTokenobj);
		String newAcessToken = jwtService.genrateAcessTokens(storedrefreshtoken.getUsers());
		String newRefreshToken = jwtService.generateRefreshToken(storedrefreshtoken.getUsers(),
				newRefreshTokenobj.getJti());

		cookieService.attachRefreshCookie(response, newRefreshToken, (int) jwtService.getRefreshtokensec());
		return ResponseEntity.ok(TokenResponse.of(newAcessToken, newRefreshToken, jwtService.getRefreshtokensec(),
				mapper.map(storedrefreshtoken.getUsers(), UsersDto.class)));
	}
	
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
	        HttpServletRequest request,
	        HttpServletResponse response) {

	    readRefreshTokenFromRequest(null, request).ifPresent(token -> {

	        if (jwtService.isRefreshToken(token)) {

	            String jti = jwtService.getJti(token);

	            refreshTokenRepos.findByJti(jti).ifPresent(refreshToken -> {

	                refreshToken.setRevoked(true);
	                refreshTokenRepos.save(refreshToken);

	            });
	        }
	    });

	    cookieService.clearRefreshCookie(response);

	    return ResponseEntity.noContent().build();
	}
	private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
		if (request.getCookies() != null) {
			Optional<String> fromcookie = Arrays.stream(request.getCookies())
					.filter(c -> cookieService.getRefreshTokenCookieName().equals(c.getName())).map(c -> c.getValue())
					.filter(v -> !v.isBlank()).findFirst();
			if (fromcookie.isPresent()) {
				return fromcookie;
			}
			if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
				return Optional.of(body.refreshToken());
			}
		}
		return Optional.empty();
	}

	private Authentication authenticate(LoginRequest loginRequest) {
		try {
			return authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
		} catch (Exception e) {
			throw new BadCredentialsException("Username Passowrd Does Not Exists");

		}

	}

}
