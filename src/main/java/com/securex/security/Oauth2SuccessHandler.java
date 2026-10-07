package com.securex.security;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.securex.entity.Provider;
import com.securex.entity.RefreshToken;
import com.securex.entity.Users;
import com.securex.repositories.RefreshTokenRepos;
import com.securex.repositories.UsersRepository;
import com.securex.services.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {

	private final UsersRepository repository;
	private final JwtService jwtService;
	private final CookieService cookieService;
	private final RefreshTokenRepos refreshTokenRepos;
	@Value("${app.oauth2.frontend-success-redirect}")
	private String frontEndSuccessUrl;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		OAuth2User oauth2user = (OAuth2User) authentication.getPrincipal();

		String registerationid = "unknown";

		if (authentication instanceof OAuth2AuthenticationToken token) {
			registerationid = token.getAuthorizedClientRegistrationId();
		}

		Users users;

		switch (registerationid) {

		case "google": {

			String googleid = oauth2user.getAttributes().getOrDefault("sub", "").toString();

			String email = oauth2user.getAttributes().getOrDefault("email", "").toString();

			String name = oauth2user.getAttributes().getOrDefault("name", "").toString();

			String picture = oauth2user.getAttributes().getOrDefault("picture", "").toString();

			Users newuser = Users.builder().email(email).name(name).image(picture).provider(Provider.GOOGLE).providerid(googleid).build();

			users = repository.findByemail(email).orElseGet(() -> repository.save(newuser));

			break;
		}

		case "github": {

			String name = oauth2user.getAttributes().getOrDefault("login", "").toString();

			String gitid = oauth2user.getAttributes().getOrDefault("id", "").toString();

			String email = oauth2user.getAttributes().get("email") != null
			        ? oauth2user.getAttributes().get("email").toString()
			        : "";

			String avtarurl = oauth2user.getAttributes().getOrDefault("avatar_url", "").toString();
			Users newuser = Users.builder().email(email).name(name).image(avtarurl).provider(Provider.GITHUB).providerid(gitid)
					.build();

			users = repository.findByemail(email).orElseGet(() -> repository.save(newuser));

			break;
		}

		default:
			throw new RuntimeException("Invalid Registration Id: " + registerationid);
		}

		String jti = UUID.randomUUID().toString();

		var refreshTokenobj = RefreshToken.builder().jti(jti).users(users).revoked(false).createdAt(Instant.now())
				.expiresAt(Instant.now().plusSeconds(jwtService.getRefreshtokensec())).build();

		refreshTokenRepos.save(refreshTokenobj);

		var accesstoken = jwtService.genrateAcessTokens(users);

		var refreshtoken = jwtService.generateRefreshToken(users, refreshTokenobj.getJti());

		cookieService.attachRefreshCookie(response, refreshtoken, (int) jwtService.getRefreshtokensec());

		// yha pr baad me frontend ka URL dalenge
		response.sendRedirect(frontEndSuccessUrl);
	}
}