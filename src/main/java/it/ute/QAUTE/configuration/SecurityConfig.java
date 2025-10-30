package it.ute.QAUTE.configuration;

import it.ute.QAUTE.dto.response.AuthenticationResponse;
import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.exception.ErrorCode;
import it.ute.QAUTE.service.Implement.AuthenticationServiceImplement;
import it.ute.QAUTE.service.Implement.SecurityServiceImplement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.net.URLEncoder;
import java.text.ParseException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    private final String[] PUBLIC_ENDPOINT = {
            "/auth/**", "/oauth2/**", "/api/**",
            "/ws/**", "/app/**", "/topic/**", "/queue/**",
            "/css/**", "/js/**", "/images/**", "/pages/block", "/app-error"
    };

    @Autowired private CustomJwtDecoder customJwtDecoder;
    @Autowired private AuthenticationServiceImplement authenticationService;
    @Autowired @Lazy private SecurityServiceImplement securityService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        .requestMatchers(PUBLIC_ENDPOINT).permitAll()
                        .requestMatchers("/user/**").hasAuthority("ROLE_User")
                        .requestMatchers("/consultant/**").hasAuthority("ROLE_Consultant")
                        .requestMatchers("/admin/**").hasAuthority("ROLE_Admin")
                        .requestMatchers("/manager/**").hasAuthority("ROLE_Manager")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/auth/login")
                        .redirectionEndpoint(r -> r.baseUri("/auth/google/callback"))
                        .userInfoEndpoint(u -> u.userService(oauth2UserService()))
                        .successHandler(oauth2SuccessHandler())
                        .failureHandler(oauth2FailureHandler())
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(customJwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                        .bearerTokenResolver(bearerTokenResolver())
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
                );
        return http.build();
    }

    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        return request -> {
            String uri = request.getRequestURI();
            if (uri.startsWith(request.getContextPath() + "/auth") ||
                    uri.startsWith(request.getContextPath() + "/oauth2") ||
                    uri.startsWith(request.getContextPath() + "/app-error")) {
                return null;
            }

            HttpSession session = request.getSession(false);
            String accessToken = null;

            if (session != null) {
                Object token = session.getAttribute("ACCESS_TOKEN");
                if (token instanceof String s && !s.isBlank()) {
                    try {
                        Jwt jwt = customJwtDecoder.decode(s);
                        Date expiry = Date.from(jwt.getExpiresAt());
                        if (expiry == null || expiry.after(new Date())) {
                            return s;
                        }
                        log.warn("Access token in session expired at {}", expiry);
                    } catch (Exception e) {
                        log.warn("Access token invalid: {}", e.getMessage());
                    }
                }
            }

            String refreshToken = extractRefreshToken(request);
            if (refreshToken != null && !refreshToken.isBlank()) {
                log.info("Access token expired, attempting refresh with cookie token...");
                try {
                    String newAccess = authenticationService.refreshAccessTokenOnly(refreshToken, request);
                    if (newAccess != null) {
                        HttpSession newSession = request.getSession(true);
                        newSession.setAttribute("ACCESS_TOKEN", newAccess);

                        log.info("Issued new access token successfully -> new session id: {}", newSession.getId());
                        return newAccess;
                    }
                } catch (Exception e) {
                    log.warn("Failed to refresh token from cookie: {}", e.getMessage());
                }
            }

            return null;
        };
    }


    private String extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(c -> "REFRESH_TOKEN".equals(c.getName()))
                        .map(Cookie::getValue)
                        .findFirst())
                .orElse(null);
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        var gac = new JwtGrantedAuthoritiesConverter();
        gac.setAuthoritiesClaimName("scope");
        gac.setAuthorityPrefix("");

        var jac = new JwtAuthenticationConverter();
        jac.setJwtGrantedAuthoritiesConverter(gac);
        return jac;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        var delegate = new DefaultOAuth2UserService();
        return delegate::loadUser;
    }

    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauthUser = oauthToken.getPrincipal();
            String email = (String) oauthUser.getAttributes().get("email");
            String deviceId = securityService.getClientIP(request);
            String deviceName = securityService.getDeviceFingerprint(request);

            log.info("OAuth2 login: {}", email);
            AuthenticationResponse auth = null;
            try {
                auth = authenticationService.authentication(Account.builder().email(email).build(), deviceId, deviceName, true);
            } catch (ParseException e) {
                redirectToError(response, request, ErrorCode.ACCOUNT_EXISTED, e.getMessage());
                return;
            }

            if (auth != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("ACCESS_TOKEN", auth.getToken());
                String role = (String) customJwtDecoder.decode(auth.getToken()).getClaims().get("scope");
                session.setAttribute("SCOPE", role);

                ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN", auth.getRefreshtoken())
                        .httpOnly(true)
                        .secure(false)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(Duration.ofDays(7))
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                response.sendRedirect(request.getContextPath() + resolveRedirectByRole("ROLE_" + auth.getRole().toString()));
            } else {
                redirectToError(response, request, ErrorCode.ACCOUNT_EXISTED, "Auth null");
            }
        };
    }

    private void redirectToError(HttpServletResponse response, HttpServletRequest request, ErrorCode code, String msg) throws java.io.IOException {
        String base = request.getContextPath();
        response.sendRedirect(base + "auth/app-error?errorCode=" + code.getCode() +
                "&message=" + URLEncoder.encode(code.getMessage() + " " + msg, java.nio.charset.StandardCharsets.UTF_8));
    }

    @Bean
    public AuthenticationFailureHandler oauth2FailureHandler() {
        return (request, response, ex) -> response.sendRedirect("/auth/login");
    }

    private String resolveRedirectByRole(String role) {
        return switch (role) {
            case "ROLE_User" -> "/user/home";
            case "ROLE_Consultant" -> "/consultant/home";
            case "ROLE_Admin" -> "/admin/users";
            case "ROLE_Manager" -> "/manager/questions";
            default -> "/auth/login";
        };
    }
}
