package com.securex.config;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.securex.security.JwtsAuthenticationFilter;
import com.securex.security.Oauth2SuccessHandler;

import tools.jackson.databind.ObjectMapper;

@Configuration
public class SecurityConfig {

    private final JwtsAuthenticationFilter authenticationFilter;
    private final Oauth2SuccessHandler oauth2SuccessHandler;

    public SecurityConfig(
            JwtsAuthenticationFilter authenticationFilter,
            Oauth2SuccessHandler oauth2SuccessHandler) {

        this.authenticationFilter = authenticationFilter;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity) throws Exception {

        httpSecurity

                // =========================
                // Disable CSRF
                // =========================
                .csrf(csrf -> csrf.disable())

                // =========================
                // CORS
                // =========================
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource()))

                // =========================
                // Stateless Session
                // =========================
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                // =========================
                // Authorization
                // =========================
                .authorizeHttpRequests(authorize -> authorize

                        // CORS Preflight
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // Authentication APIs
                        .requestMatchers(
                                "/api/v1/auth/**"
                        ).permitAll()

                        // Error endpoint
                        .requestMatchers(
                                "/error"
                        ).permitAll()

                        // GET APIs
                        .requestMatchers(
                                HttpMethod.GET
                        ).hasRole(AppConstent.GUEST_ROLE)

                        // Admin APIs
                        .requestMatchers(
                                "/api/v1/users/**"
                        ).hasRole(AppConstent.ADMIN_ROLE)

                        // Everything else
                        .anyRequest().authenticated())

                // =========================
                // OAuth2 Login
                // =========================
                .oauth2Login(oauth2 ->
                        oauth2
                                .successHandler(oauth2SuccessHandler)
                                .failureHandler(null))

                // =========================
                // Disable Default Logout
                // =========================
                .logout(logout ->
                        logout.disable())

                // =========================
                // Exception Handling
                // =========================
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(
                                (request, response, ae) -> {

                                    ae.printStackTrace();

                                    response.setStatus(401);
                                    response.setContentType(
                                            "application/json");

                                    String msg =
                                            "Unauthorized Access! "
                                            + ae.getMessage();

                                    Map<String, String> errorMap =
                                            Map.of(
                                                    "msg",
                                                    msg,
                                                    "status",
                                                    "401",
                                                    "statusCode",
                                                    "401"
                                            );

                                    var objectMapper =
                                            new ObjectMapper();

                                    response.getWriter().write(
                                            objectMapper.writeValueAsString(
                                                    errorMap));
                                }))

                // =========================
                // JWT Authentication Filter
                // =========================
                .addFilterBefore(
                        authenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    // =========================
    // CORS CONFIGURATION
    // =========================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config =
                new CorsConfiguration();

        // Local React frontend + Production Vercel frontend
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://securex-frontend-phi.vercel.app"
        ));

        // Allowed HTTP methods
        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        // Allow all request headers
        config.setAllowedHeaders(List.of("*"));

        // Required because frontend uses credentials/cookies
        config.setAllowCredentials(true);

        // Register CORS for all endpoints
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                config
        );

        return source;
    }

    // =========================
    // PASSWORD ENCODER
    // =========================

    @Bean
    public PasswordEncoder encoder() {

        return new BCryptPasswordEncoder();
    }

    // =========================
    // AUTHENTICATION MANAGER
    // =========================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration)
            throws Exception {

        return authenticationConfiguration
                .getAuthenticationManager();
    }
}