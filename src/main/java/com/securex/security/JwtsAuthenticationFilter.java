package com.securex.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.securex.helper.UserHelper;
import com.securex.repositories.UsersRepository;
import com.securex.services.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtsAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UsersRepository repository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer")) {
			String token = header.substring(7);
			if (!jwtService.isAccessToken(token)) {
				filterChain.doFilter(request, response);
				return;
			}
			try {
				Jws<Claims> parse = jwtService.parse(token);
				Claims payload = parse.getPayload();
				String userid = payload.getSubject();
				UUID UserUuid = UserHelper.parseUuid(userid);
				repository.findById(UserUuid).ifPresent(user -> {
					if (user.isEnable()) {

						List<GrantedAuthority> authorities = user.getRoles().stream()
								.map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());

						UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
								user.getEmail(), null, authorities);
						authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						if (SecurityContextHolder.getContext().getAuthentication() == null)
							SecurityContextHolder.getContext().setAuthentication(authenticationToken);

					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		filterChain.doFilter(request, response);

	}
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return request.getRequestURI().startsWith("/api/v1/auth");
	}

}
