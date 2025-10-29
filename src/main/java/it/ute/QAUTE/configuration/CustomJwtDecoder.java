package it.ute.QAUTE.configuration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import it.ute.QAUTE.entity.RefreshToken;
import it.ute.QAUTE.repository.RefreshTokenRepository;
import it.ute.QAUTE.service.Implement.AuthenticationServiceImplement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Date;

@Slf4j
@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey_access}")
    private String signerKey;

    @Autowired
    private AuthenticationServiceImplement authenticationService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            if (token == null || token.isBlank()) {
                throw new JwtException("Empty token received");
            }

            SignedJWT signedJWT = SignedJWT.parse(token);
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            String tokenType = signedJWT.getJWTClaimsSet().getStringClaim("type");
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            Date now = new Date();

            String activeSignKey = signerKey;

            if ("refresh".equals(tokenType)) {
                RefreshToken refreshToken = refreshTokenRepository
                        .findById(jti)
                        .orElseThrow(() -> new JwtException("Refresh token not found"));
                activeSignKey = refreshToken.getSignKey();
            }

            if ("access".equals(tokenType) && expiryTime != null && expiryTime.before(now)) {
                log.warn("Access token expired at {} → clearing session...", expiryTime);
                invalidateSession();
                throw new JwtException("Access token expired");
            }

            SignedJWT verified = authenticationService.verifyToken(token);
            if (verified == null) {
                throw new JwtException("Token verification failed");
            }

            SecretKeySpec secretKeySpec = new SecretKeySpec(activeSignKey.getBytes(), "HmacSHA512");
            NimbusJwtDecoder decoder = NimbusJwtDecoder
                    .withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();

            return decoder.decode(token);

        } catch (ParseException e) {
            log.error("Token parse failed: {}", e.getMessage());
            throw new JwtException("Invalid token format");
        } catch (JOSEException e) {
            log.error("Token verification error: {}", e.getMessage());
            throw new JwtException("Token verification failed");
        }
    }

    private void invalidateSession() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                    log.info("✅ Session invalidated because access token expired.");
                }
            }
        } catch (Exception e) {
            log.error("Failed to invalidate session: {}", e.getMessage());
        }
    }
}
