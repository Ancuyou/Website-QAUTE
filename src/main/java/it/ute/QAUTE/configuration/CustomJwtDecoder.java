package it.ute.QAUTE.configuration;

import com.nimbusds.jwt.SignedJWT;
import it.ute.QAUTE.service.Implement.AuthenticationServiceImplement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@Slf4j
@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey_access}")
    private String signerKey;

    @Autowired
    private AuthenticationServiceImplement authenticationService;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String tokenType = signedJWT.getJWTClaimsSet().getStringClaim("type");
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean expired = "access".equals(tokenType)
                    && expiryTime != null
                    && expiryTime.before(new Date());

            if (expired) {
                log.warn("Access token expired at {}", expiryTime);
                // ⚠️ Không verify token nữa nếu đã hết hạn
            } else {
                // ✅ Chỉ verify khi token còn hạn
                SignedJWT verified = authenticationService.verifyToken(token);
                if (verified == null) {
                    throw new JwtException("Token verification failed");
                }
            }

            // ✅ Decode signature thông thường
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HmacSHA512");
            NimbusJwtDecoder decoder = NimbusJwtDecoder
                    .withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();

            return decoder.decode(token);

        } catch (Exception e) {
            log.error("Token invalid or cannot be decoded: {}", e.getMessage());
            throw new JwtException("Token invalid: " + e.getMessage());
        }
    }
}
