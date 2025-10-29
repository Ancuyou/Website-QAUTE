package it.ute.QAUTE.configuration;

import it.ute.QAUTE.dto.response.AuthenticationResponse;
import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.exception.ErrorCode;
import it.ute.QAUTE.service.Implement.AuthenticationServiceImplement;
import it.ute.QAUTE.service.Implement.SecurityServiceImplement;
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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    // === PUBLIC ENDPOINTS ===
    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/**", "/oauth2/**", "/ws/**", "/app/**", "/topic/**",
            "/queue/**", "/api/**", "/app-error/**", "/notifications/**",
            "/css/**", "/js/**", "/images/**", "/pages/block"
    };

    // === ROLE-BASED ENDPOINTS ===
    private static final String[] USER_ENDPOINTS = {"/user/questions/**", "/user/**"};
    private static final String[] CONSULTANT_ENDPOINTS = {"/consultant/**"};
    private static final String[] ADMIN_ENDPOINTS = {"/admin/**"};
    private static final String[] MANAGER_ENDPOINTS = {"/manager/**"};

    @Autowired private CustomJwtDecoder customJwtDecoder;
    @Autowired private AuthenticationServiceImplement authenticationService;
    @Autowired @Lazy private SecurityServiceImplement securityService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers("/images/**").permitAll()

                        // Shared between User & Consultant
                        .requestMatchers("/user/questions/**").hasAnyAuthority("ROLE_User", "ROLE_Consultant")

                        // Role-specific
                        .requestMatchers(USER_ENDPOINTS).hasAuthority("ROLE_User")
                        .requestMatchers(CONSULTANT_ENDPOINTS).hasAuthority("ROLE_Consultant")
                        .requestMatchers(ADMIN_ENDPOINTS).hasAuthority("ROLE_Admin")
                        .requestMatchers(MANAGER_ENDPOINTS).hasAuthority("ROLE_Manager")

                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/login")
                        .redirectionEndpoint(r -> r.baseUri("/auth/google/callback"))
                        .userInfoEndpoint(u -> u.userService(oAuth2UserService()))
                        .successHandler(oAuth2SuccessHandler())
                        .failureHandler(oAuth2FailureHandler())
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

    // === BEARER TOKEN RESOLVER (Session + Cookie) ===
    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        return request -> {
            String uri = request.getRequestURI();
            String contextPath = request.getContextPath();

            // Bỏ qua các endpoint không cần token
            if (isPublicAuthEndpoint(uri, contextPath)) {
                return null;
            }

            // 1. Kiểm tra session
            HttpSession session = request.getSession(false);
            if (session != null) {
                String token = (String) session.getAttribute("ACCESS_TOKEN");
                if (token != null && !token.isBlank()) {
                    return token;
                }
            }

            // 2. Kiểm tra cookie REFRESH_TOKEN
            var cookies = request.getCookies();
            if (cookies != null) {
                for (var cookie : cookies) {
                    if ("REFRESH_TOKEN".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                        return cookie.getValue();
                    }
                }
            }

            return null;
        };
    }

    private boolean isPublicAuthEndpoint(String uri, String contextPath) {
        return uri.startsWith(contextPath + "/auth") ||
                uri.startsWith(contextPath + "/oauth2") ||
                uri.startsWith(contextPath + "/app-error") ||
                uri.startsWith(contextPath + "/auth/google/callback");
    }

    // === JWT AUTHENTICATION CONVERTER ===
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("scope");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    // === PASSWORD ENCODER ===
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    // === OAUTH2 USER SERVICE ===
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return delegate::loadUser;
    }

    // === OAUTH2 SUCCESS HANDLER ===
    @Bean
    public AuthenticationSuccessHandler oAuth2SuccessHandler() {
        return (request, response, authentication) -> {
            try {
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                OAuth2User oauthUser = oauthToken.getPrincipal();
                String email = (String) oauthUser.getAttributes().get("email");

                String deviceId = securityService.getClientIP(request);
                String deviceName = securityService.getDeviceFingerprint(request);

                AuthenticationResponse auth = authenticationService.authentication(
                        Account.builder().email(email).build(),
                        deviceId, deviceName, true
                );

                if (auth == null || auth.getToken() == null) {
                    log.error("OAuth2 login failed: authentication response is invalid");
                    redirectToError(request, response, ErrorCode.ACCOUNT_EXISTED);
                    return;
                }

                // Lưu token vào session
                HttpSession session = request.getSession(true);
                session.setAttribute("ACCESS_TOKEN", auth.getToken());

                // Xác định role từ JWT hoặc fallback
                String role = extractRoleFromToken(auth.getToken(), auth.getRole().toString());
                session.setAttribute("SCOPE", role);

                // Thiết lập cookie refresh token
                ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN", auth.getRefreshtoken())
                        .httpOnly(true)
                        .secure(true) // Nên bật nếu dùng HTTPS
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(Duration.ofDays(7))
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                // Chuyển hướng theo role
                String redirectPath = resolveRedirectPath(role);
                response.sendRedirect(request.getContextPath() + redirectPath);

            } catch (Exception e) {
                log.error("OAuth2 success handler failed", e);
                redirectToError(request, response, ErrorCode.ACCOUNT_EXISTED);
            }
        };
    }

    // === OAUTH2 FAILURE HANDLER ===
    @Bean
    public AuthenticationFailureHandler oAuth2FailureHandler() {
        return (request, response, ex) -> redirectToError(request, response, ErrorCode.ACCOUNT_EXISTED);
    }

    // === HELPER: Redirect to error page ===
    private void redirectToError(jakarta.servlet.http.HttpServletRequest request,
                                 jakarta.servlet.http.HttpServletResponse response,
                                 ErrorCode errorCode) throws java.io.IOException {
        String base = request.getContextPath();
        String encodedMessage = URLEncoder.encode(errorCode.getMessage(), StandardCharsets.UTF_8);
        response.sendRedirect(base + "/app-error?errorCode=" + errorCode.getCode() + "&message=" + encodedMessage);
    }

    // === HELPER: Extract role from JWT or fallback ===
    private String extractRoleFromToken(String token, String fallbackRole) {
        try {
            var decoded = customJwtDecoder.decode(token);
            return (String) decoded.getClaims().get("scope");
        } catch (Exception e) {
            log.warn("Failed to decode JWT for role, using fallback: {}", fallbackRole);
            return "ROLE_" + fallbackRole;
        }
    }

    // === HELPER: Resolve redirect path by role ===
    private String resolveRedirectPath(String role) {
        return switch (role) {
            case "ROLE_User" -> "/user/home";
            case "ROLE_Consultant" -> "/consultant/home";
            case "ROLE_Admin" -> "/admin/users";
            case "ROLE_Manager" -> "/manager/questions";
            default -> "/auth/login";
        };
    }
}