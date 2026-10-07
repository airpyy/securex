package com.securex.security;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

@Service
@Getter
public class CookieService {
	private final String refreshTokenCookieName;
	private final boolean cookieHttpOnly;
	private final boolean cookieSecure;
	private final String cookieDomain;
	private final String cookieSameSite;

	public CookieService(@Value("${security.jwt.refresh-token-cookie-name}") String refreshTokenCookieName,
			@Value("${security.jwt.http-only}") boolean cookieHttpOnly,
			@Value("${security.jwt.cookie-secure}") boolean cookieSecure,
			@Value("${security.jwt.cookie-domain}") String cookieDomain,
			@Value("${security.jwt.cookie-same-site}") String cookieSameSite) {
		this.refreshTokenCookieName = refreshTokenCookieName;
		this.cookieHttpOnly = cookieHttpOnly;
		this.cookieSecure = cookieSecure;
		this.cookieDomain = cookieDomain;
		this.cookieSameSite = cookieSameSite;
	}

	public void attachRefreshCookie(HttpServletResponse httpServletResponse ,String value , int maxAge) {
		 var responseCookieBuilder = ResponseCookie.from(refreshTokenCookieName,value)
		.httpOnly(cookieHttpOnly)
		.secure(cookieSecure)
		.path("/")
		.maxAge(maxAge)
		.sameSite(cookieSameSite);
		 
		 if(cookieDomain!=null && !cookieDomain.isBlank()) {
			 responseCookieBuilder.domain(cookieDomain);
		 }
		  ResponseCookie responsecoo =responseCookieBuilder.build();
		  httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, responsecoo.toString());
	}
	//logout krte waqt cookie clear ho jayegi
	public void clearRefreshCookie(HttpServletResponse response) {
		  var builder=ResponseCookie.from(refreshTokenCookieName,"")
		.maxAge(0)
		.httpOnly(cookieHttpOnly)
		.path("/")
		.sameSite(cookieSameSite)
		.secure(cookieSecure);
		if(cookieDomain!=null && cookieDomain.isBlank()) {
		     builder.domain(cookieDomain);
		}
		  ResponseCookie responsecoo =builder.build();
		  response.addHeader(HttpHeaders.SET_COOKIE, responsecoo.toString());
	}

}
