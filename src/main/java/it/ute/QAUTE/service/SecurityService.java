package it.ute.QAUTE.service;

import com.github.benmanes.caffeine.cache.Cache;
import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.BlackList;
import it.ute.QAUTE.repository.AccountRepository;
import it.ute.QAUTE.repository.BlackListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class SecurityService {
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
    private static final int ramThreshold = 4;
    private static final int dbLockThreshold=2;
    private static final int downgradeCircle=7;
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
    public Account unlock(Account account){
        if(account.getSecurityLevel()>=2 && account.getLockUntil()!=null && account.getLockUntil().before(new Date()) && account.isBlock()){
            System.out.println("đã mở khoá");
            account.setBlock(false);
            account.setLockUntil(null);
        }
        return account;
    }
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
    private void lockOnRAM(String username, int level) {
        LocalDateTime lockUntil=levelLockTime(level);
        long lockUntilMillis = lockUntil.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        temporaryLockCache.put(username, lockUntilMillis);
        long lockMinutes = Duration.between(LocalDateTime.now(), lockUntil).toMinutes();
        System.out.println(String.format("🔒 KHÓA TRÊN RAM - %s (Level %d) - Khóa %d phút (đến %s)",
                username, level, lockMinutes, lockUntil.format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
    }
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
    public void loginFailed(String username,String deviceId,String deviceName){
        Map<String, Integer> usernameAttempts = deviceAttemptCache.getIfPresent(deviceId);
        if (usernameAttempts== null) {
            usernameAttempts = new HashMap<>();
        }
        usernameAttempts.put(username, usernameAttempts.getOrDefault(username, 0) + 1);
        deviceAttemptCache.put(deviceId, usernameAttempts);
        handleBlockDevice(deviceId,deviceName);
    }
    public void handleBlockDevice(String deviceId,String deviceName){
        System.out.println("gọi khoá thiết bị");
        Map<String, Integer> usernameAttempts = deviceAttemptCache.getIfPresent(deviceId);
        List<String> targetUsernames = new ArrayList<>(usernameAttempts.keySet());
        Long failCount=usernameAttempts.values().stream().filter(count -> count >= 0).count();
        boolean isBLock=false;
        if(targetUsernames.size()==1 && failCount>=1) isBLock=true;
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
            // có thể tích hợp gửi email cho admin
        }
    }
    public boolean isDeviceBlock(String deviceId,String deviceName){
        BlackList blackList=blackListRepository.findByDeviceIdAndDeviceName(deviceId,deviceName);
        if (blackList!=null){
            blackList=unblockDevice(blackList);
            if(blackList.isBlock())return true;
        }
        return false;
    }
    public BlackList unblockDevice(BlackList blackList){
        if(blackList.getUnblockAt()!=null&&blackList.getUnblockAt().before(new Date())){
            blackList.setUnblockAt(null);
            blackList.setBlock(false);
            return blackListRepository.save(blackList);
        }
        return blackList;
    }
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
}
