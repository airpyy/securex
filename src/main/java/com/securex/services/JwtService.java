package com.securex.services;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.securex.entity.Users;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;

@Service
@Getter
@Setter
public class JwtService {
	private final SecretKey key;
	private final long accessTokenSec;
	private final long refreshtokensec;
	private final String issuer;

	public JwtService(@Value("${security.jwt.secret}") String key, 
			@Value("${security.jwt.issuer}") String issuer,
			@Value("${security.jwt.access-token-seconds}") long acesstokensec,
			@Value("${security.jwt.refresh-token-seconds}") long refreshtokensec) {
		if (key == null || key.length() < 64) {
			throw new IllegalArgumentException("Invalid Security Key");
		}
		this.key = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
		this.refreshtokensec = refreshtokensec;
		this.accessTokenSec = acesstokensec;
		this.issuer = issuer;
	}

	public String genrateAcessTokens(Users users) {
		Instant now = Instant.now();
		List<String> roles = users.getRoles() == null ? List.of()
				: users.getRoles().stream().map(Role -> Role.getName()).toList();
		return Jwts.builder().id(UUID.randomUUID().toString()).subject(users.getId().toString()).issuer(issuer)
				.issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(accessTokenSec)))
				.claims(Map.of("email", users.getEmail(), "roles", roles, "type", "access"

				)).signWith(key, SIG.HS512).compact();

	}
    public String generateRefreshToken(Users users,String jti) {
    	Instant now = Instant.now();
    	return Jwts.builder().id(jti)
    			.subject(users.getId().toString())
    			.issuer(issuer)
    			.issuedAt(Date.from(now))
    			.expiration(Date.from(now.plusSeconds(accessTokenSec)))
    			.claim("type", "refresh")
    			.signWith(key,Jwts.SIG.HS512)
    			.compact();
    }
    public Jws<Claims>parse(String token){
    	return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }
    
    public Boolean isAccessToken(String token) {
    	Claims c = parse(token).getPayload();
    	return "access".equals(c.get("type"));
    }
    
    public Boolean isRefreshToken(String token) {
    	Claims c = parse(token).getPayload();
    	return "refresh".equals(c.get("type"));
    }
    public UUID getUserID(String token) {
    	Claims c = parse(token).getPayload();
    	return UUID.fromString(c.getSubject());
    }
    public String getJti(String token) {
    	return parse(token).getPayload().getId();
    }
    public String getEmail(String token) {
    	Claims c = parse(token).getPayload();
    	return (String)c.get("email");
    }
    public List<String> getRoles(String token){
    	Claims c =parse(token).getPayload();
    	return (List<String>) c.get("roles");
    }
}
