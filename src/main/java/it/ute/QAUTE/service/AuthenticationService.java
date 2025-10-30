package it.ute.QAUTE.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import it.ute.QAUTE.dto.response.AuthenticationResponse;
import it.ute.QAUTE.dto.response.MFAResponse;
import it.ute.QAUTE.dto.response.RefreshTokenResponse;
import it.ute.QAUTE.entity.Account;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.text.ParseException;

public interface AuthenticationService {
    boolean check(String text, String hasedText);

    String hashed(String text);

    AuthenticationResponse authentication(Account account, String deviceId, String name_device, boolean isGoogle) throws ParseException;

    String builtScope(Account account);

    String generateToken(Account account, String signKey, boolean isRefresh);

    SignedJWT verifyToken(String token) throws JOSEException, ParseException;

    void logout(String token, String tokenRefresh) throws ParseException, JOSEException;

    String forgetPassword(String email);

    String register(String username, String email);

    String changePassword(String email);

    String MFA(String email);

    // Func call in func Authenticated after check user, pass
    RefreshTokenResponse refreshToken(Account account, String deviceName) throws ParseException;

    String generateSignMaxSecurity();

    String fallbackSecureRandom128();

    String toHex(byte[] bytes);

    int getCurrentUserId(Object tokenObj, HttpServletRequest request, HttpServletResponse response) throws ParseException, JOSEException;

    Account getCurrentAccount();

    String refreshAccessTokenOnly(String refreshToken, HttpServletRequest request);

    String createMFACache(MFAResponse mfaResponse);

    MFAResponse get(String cid);
}
