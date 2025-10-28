package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.BlackList;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

public interface SecurityService {
    void initData(String username);

    void handleFailedLogin(String username);

    Account unlock(Account account);

    void reduceLevelSecurity(Account account);

    void lockOnRAM(String username, int level);

    String isAccountLocked(Account account);

    void loginFailed(String username, String deviceId, String deviceName);

    void handleBlockDevice(String deviceId, String deviceName);

    boolean isDeviceBlock(String deviceId, String deviceName);

    boolean unblockDevice(BlackList blackList);

    LocalDateTime levelLockTime(int level);

    String getClientIP(HttpServletRequest request);

    String getDeviceFingerprint(HttpServletRequest request);
}
