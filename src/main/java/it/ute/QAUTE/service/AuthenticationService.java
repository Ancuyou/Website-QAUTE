package it.ute.QAUTE.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import it.ute.QAUTE.Exception.AppException;
import it.ute.QAUTE.Exception.ErrorCode;
import it.ute.QAUTE.dto.response.AuthenticationResponse;
import it.ute.QAUTE.entity.InvalidatedToken;
import it.ute.QAUTE.entity.User;
import it.ute.QAUTE.repository.InvalidatedTokenRepository;
import it.ute.QAUTE.repository.UserReponsitory;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@Slf4j
public class AuthenticationService {
    @Autowired
    UserReponsitory userReponsitory;
    @Autowired
    InvalidatedTokenRepository invalidatedTokenRepository;
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;



    public AuthenticationResponse authentication(User user) {
        User userRep = userReponsitory.findByUsername(user.getUsername());
        if (userRep == null) {
            return AuthenticationResponse.builder()
                    .authenticated(false)
                    .build();
        } else {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
            boolean authenticated = passwordEncoder.matches(user.getPassword(),
                    userRep.getPassword());
            if (authenticated)
                return AuthenticationResponse.builder()
                        .authenticated(true)
                        .token(generateToken(userRep))
                        .build();
        }
        return AuthenticationResponse.builder()
                .authenticated(false)
                .build();
    }
    private String builtScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" ");
        stringJoiner.add("ROLE_" + user.getRole().getRoleName());
        return stringJoiner.toString();
    }
    private String generateToken(User user){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("qaute.com")
                .issueTime(new Date())                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", builtScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try{
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e){
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }
    public SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expityTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expityTime.after(new Date())))
            throw new AppException(ErrorCode.INVALID_TOKEN);
        if ( invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.TOKEN_REVOKED);
        }
        // sau viet them code refesh token o day

        return signedJWT;
    }
    public void logout(String token){
        try {
            var signToken = verifyToken(token);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().invalidatedTokenId(jit)
                                        .expiryTime(expiryTime)
                                        .build();
            invalidatedTokenRepository.save(invalidatedToken);
        } catch (ParseException | JOSEException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }
}

