package it.ute.QAUTE.service.Implement;

import com.github.benmanes.caffeine.cache.Cache;
import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.BlackList;
import it.ute.QAUTE.repository.AccountRepository;
import it.ute.QAUTE.repository.BlackListRepository;
import it.ute.QAUTE.service.EmailService;
import it.ute.QAUTE.service.NotificationService;
import it.ute.QAUTE.service.SecurityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class SecurityServiceImplement implements SecurityService {
    @Autowired
    @Qualifier("securityLimiterCache")
    private Cache<String, Map<String, Integer>>securityLimiterCache;
    @Autowired
    private Cache<String, Long> temporaryLockCache;
    @Autowired
    @Qualifier("deviceAttemptCache")
    private Cache<String, Map<String, Integer>> deviceAttemptCache;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private BlackListRepository blackListRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private NotificationService notificationService;
    private static final int ramThreshold = 4;
    private static final int dbLockThreshold=2;
    private static final int downgradeCircle=7;
    @Override
    public void initData(String username){
        Map<String, Integer> data = securityLimiterCache.getIfPresent(username);
        Account account=accountRepository.findByUsername(username);
        if(data==null){
            data = new HashMap<>();
            data.put("attempts", 0);
            data.put("level", account.getSecurityLevel());
            securityLimiterCache.put(username, data);
        }
    }
    @Override
    public void handleFailedLogin(String username){
        initData(username);
        Map<String, Integer> data = securityLimiterCache.getIfPresent(username);
        Account account=accountRepository.findByUsername(username);
        int attempts = data.get("attempts");
        int level = data.get("level");
        if(attempts<ramThreshold){
            attempts++;
        }else {
            if(level>=dbLockThreshold){
                LocalDateTime lockTime=levelLockTime(level);
                account.setLockUntil(Date.from(lockTime.atZone(ZoneId.systemDefault()).toInstant()));
                account.setBlock(true);
                System.out.println("🔒 ĐÃ KHÓA TÀI KHOẢN - " + username + " đến " + lockTime);
                account.setSecurityLevel(level);
                account.setLevelEventAt(new Date());
                accountRepository.save(account);
            }else {
                lockOnRAM(username,level);
                attempts=0;
            }
            if (level<7)level++;
        }
        System.out.println("level: "+level+" attempts: "+attempts);
        data.put("attempts", attempts);
        data.put("level", level);
        securityLimiterCache.put(username, data);
    }
    @Override
    public Account unlock(Account account){
        if(account.getSecurityLevel()>=2 && account.getLockUntil()!=null && account.getLockUntil().before(new Date()) && account.isBlock()){
            System.out.println("đã mở khoá");
            account.setBlock(false);
            account.setLockUntil(null);
        }
        return account;
    }
    @Override
    public void reduceLevelSecurity(Account account){
        initData(account.getUsername());
        if (account.getLevelEventAt() == null) {
            Map<String, Integer> data = securityLimiterCache.getIfPresent(account.getUsername());
            Long lockUntilMillis = temporaryLockCache.getIfPresent(account.getUsername());
            if(data!=null && lockUntilMillis!=null){
                if (System.currentTimeMillis() >= lockUntilMillis) {
                    temporaryLockCache.invalidate(account.getUsername());
                    int level = data.get("level");
                    if (level>0)data.put("level", level-1);
                }
            }
            data.put("attempts", 0);
            securityLimiterCache.put(account.getUsername(), data);
            return;
        }
        Date eventAt = new Date(account.getLevelEventAt().getTime() + TimeUnit.DAYS.toMillis(downgradeCircle));;
        if(eventAt.before(new Date())) {
            if (account.getSecurityLevel() == 2) {
                account.setSecurityLevel(0);
                account.setLevelEventAt(null);
            } else if (account.getSecurityLevel() > 0) {
                account.setSecurityLevel(account.getSecurityLevel() - 1);
                account.setLevelEventAt(new Date());
            }
            System.out.println("level: "+account.getSecurityLevel());
            accountRepository.save(account);
            Map<String, Integer> data = securityLimiterCache.getIfPresent(account.getUsername());
            if (data != null) {
                data.put("level", account.getSecurityLevel());
                data.put("attempts", 0);
                securityLimiterCache.put(account.getUsername(), data);
            }
        }
    }
    @Override
    public void lockOnRAM(String username, int level) {
        LocalDateTime lockUntil=levelLockTime(level);
        long lockUntilMillis = lockUntil.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        temporaryLockCache.put(username, lockUntilMillis);
        long lockMinutes = Duration.between(LocalDateTime.now(), lockUntil).toMinutes();
        System.out.println(String.format("🔒 KHÓA TRÊN RAM - %s (Level %d) - Khóa %d phút (đến %s)",
                username, level, lockMinutes, lockUntil.format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
    }
    @Override
    public String isAccountLocked(Account account){
        initData(account.getUsername());
        Long lockUntilMillis = temporaryLockCache.getIfPresent(account.getUsername());
        if(lockUntilMillis!=null){
            if (System.currentTimeMillis() < lockUntilMillis) {
                long remainingMinutes = (lockUntilMillis - System.currentTimeMillis()) / 60000;
                return String.format("⏳ Đã khóa tài khoản - %s còn %d phút", account.getUsername(), remainingMinutes);
            } else {
                temporaryLockCache.invalidate(account.getUsername());
                System.out.println(String.format("🔓 Hết hạn khóa RAM - %s", account.getUsername()));
            }
        }
        account=unlock(account);
        if(account.isBlock()){
            if (account.getLockUntil() != null) {
                LocalDateTime lockUntil = LocalDateTime.ofInstant(account.getLockUntil().toInstant(), ZoneId.systemDefault());
                long lockMinutes = Duration.between(LocalDateTime.now(), lockUntil).toMinutes();
                return String.format("⏳ Đã khóa tài khoản - %s còn %d phút", account.getUsername(), lockMinutes);
            }
            return "Tài khoản đã bị khoá";
        }
        accountRepository.save(account);
        return "";
    }
    @Override
    public void loginFailed(String username, String deviceId, String deviceName){
        Map<String, Integer> usernameAttempts = deviceAttemptCache.getIfPresent(deviceId);
        if (usernameAttempts== null) {
            usernameAttempts = new HashMap<>();
        }
        usernameAttempts.put(username, usernameAttempts.getOrDefault(username, 0) + 1);
        deviceAttemptCache.put(deviceId, usernameAttempts);
        handleBlockDevice(deviceId,deviceName);
    }
    @Override
    public void handleBlockDevice(String deviceId, String deviceName){
        Map<String, Integer> usernameAttempts = deviceAttemptCache.getIfPresent(deviceId);
        List<String> targetUsernames = new ArrayList<>(usernameAttempts.keySet());
        int failCount=usernameAttempts.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        boolean isBLock=false;
        StringBuilder reason= new StringBuilder("Danh sách các tên đăng nhập của các tài khoản bị tấn công là: ");
        for (String targetUsername : targetUsernames){
            reason.append(targetUsername).append(", ");
        }
        reason.append("\nVui lòng thực hiện việc khóa tài khoản hoặc đưa ra các giải pháp phù hợp");
        if(targetUsernames.size()==1 && failCount>=10) isBLock=true;
        else if(targetUsernames.size()==2 && failCount>=15) isBLock=true;
        else if(targetUsernames.size()>=3 && failCount>=15) isBLock=true;
        if(isBLock){
            System.out.println("khoá thiết bị");
            BlackList newBlock=new BlackList();
            LocalDateTime unblockAt=LocalDateTime.now().plusHours(8);
            newBlock.setUnblockAt(Date.from(unblockAt.atZone(ZoneId.systemDefault()).toInstant()));
            newBlock.setBlock(true);
            newBlock.setDeviceId(deviceId);
            newBlock.setBlockAt(new Date());
            newBlock.setDeviceName(deviceName);
            blackListRepository.save(newBlock);
            List<Account> adminList=accountRepository.findAllAdmin();
            for (Account admin:adminList){
                emailService.sendSuspiciousActivityAlert(admin.getEmail(),deviceId,deviceName,"Đang thực hiện đăng nhập nhiều đăng nhập",reason.toString());
            }
            notificationService.notifyAdminSuspiciousActivityAlert(deviceId,deviceName,"Đang thực hiện đăng nhập nhiều đăng nhập",reason.toString());
        }
    }
    @Override
    public boolean isDeviceBlock(String deviceId, String deviceName){
        BlackList blackList=blackListRepository.findByDeviceIdAndDeviceName(deviceId,deviceName);
        if (blackList!=null){
            boolean unlock=unblockDevice(blackList);
            if(!unlock)return true;
        }
        return false;
    }
    @Override
    public boolean unblockDevice(BlackList blackList){
        if(blackList.getUnblockAt()!=null&&blackList.getUnblockAt().before(new Date())){
            blackListRepository.deleteByDeviceIdAndDeviceName(blackList.getDeviceId(),blackList.getDeviceName());
            return true;
        }
        return false;
    }
    @Override
    public LocalDateTime levelLockTime(int level){
        return switch (level) {
            case 0 -> LocalDateTime.now().plusMinutes(1);
            case 1 -> LocalDateTime.now().plusMinutes(5);
            case 2 -> LocalDateTime.now().plusHours(1);
            case 3 -> LocalDateTime.now().plusHours(3);
            case 4 -> LocalDateTime.now().plusHours(8);
            case 5 -> LocalDateTime.now().plusDays(1);
            case 6 -> LocalDateTime.now().plusDays(7);
            case 7 -> LocalDateTime.now().plusMonths(1);
            default -> null;
        };
    }
    @Override
    public String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty() && !"unknown".equalsIgnoreCase(xRealIP)) {
            return xRealIP;
        }
        String proxyClientIP = request.getHeader("Proxy-Client-IP");
        if (proxyClientIP != null && !proxyClientIP.isEmpty() && !"unknown".equalsIgnoreCase(proxyClientIP)) {
            return proxyClientIP;
        }
        String wlProxyClientIP = request.getHeader("WL-Proxy-Client-IP");
        if (wlProxyClientIP != null && !wlProxyClientIP.isEmpty() && !"unknown".equalsIgnoreCase(wlProxyClientIP)) {
            return wlProxyClientIP;
        }
        String httpClientIP = request.getHeader("HTTP_CLIENT_IP");
        if (httpClientIP != null && !httpClientIP.isEmpty() && !"unknown".equalsIgnoreCase(httpClientIP)) {
            return httpClientIP;
        }
        String httpXForwardedFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (httpXForwardedFor != null && !httpXForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(httpXForwardedFor)) {
            return httpXForwardedFor.split(",")[0].trim();
        }
        String remoteAddr = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr)) {
            return "127.0.0.1";
        }
        return remoteAddr;
    }
    @Override
    public String getDeviceFingerprint(HttpServletRequest request) {
        StringBuilder fingerprint = new StringBuilder();

        // User-Agent
        String userAgent = request.getHeader("User-Agent");
        fingerprint.append(userAgent != null ? userAgent : "Unknown");

        // Accept-Language
        String language = request.getHeader("Accept-Language");
        if (language != null) {
            fingerprint.append("|").append(language);
        }

        // Screen resolution từ custom header (cần frontend gửi lên)
        String screenRes = request.getHeader("X-Screen-Resolution");
        if (screenRes != null) {
            fingerprint.append("|").append(screenRes);
        }

        // Hash để rút ngắn
        return String.valueOf(fingerprint.toString().hashCode());
    }
}
