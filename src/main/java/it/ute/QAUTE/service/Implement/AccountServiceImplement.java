package it.ute.QAUTE.service.Implement;

import it.ute.QAUTE.entity.*;
import it.ute.QAUTE.exception.AppException;
import it.ute.QAUTE.exception.ErrorCode;
import com.github.benmanes.caffeine.cache.Cache;
import it.ute.QAUTE.repository.*;
import it.ute.QAUTE.service.AccountService;
import it.ute.QAUTE.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@Service
public class AccountServiceImplement implements AccountService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AuthenticationServiceImplement authenticationService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private Cache<Integer, Boolean> onlineCache;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private ProfilesRepository profilesRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private ManagerRepository managerRepository;
    @Autowired
    private ConsultantRepository consultantRepository;
    @Autowired
    private UserRepository userRepository;
    @Override
    public void changePassword(String email, String password){
        Account account=accountRepository.findByEmail(email);
        account.setPassword(authenticationService.hashed(password));
        accountRepository.save(account);
    }
    @Override
    public Account findById(int id){
        return accountRepository.findByAccountID(id);
    }
    @Override
    public void updateAccount(Account account){
        accountRepository.save(account);
    }
    @Override
    public void createAccount(String username, String password, String email) {
        Profiles profiles = new Profiles();
        profiles.setFullName("user" + (1000 + new Random().nextInt(9000)));
        profiles.setPhone("0000000000");
        profiles.setAvatar(null);
        Account account = new Account();
        account.setUsername(username);
        account.setEmail(email);
        account.setPassword(authenticationService.hashed(password));
        account.setRole(Account.Role.User);
        account.setCreatedDate(new Date());
        account.setProfile(profiles);
        User user = new User();
        user.setStudentCode("123");
        user.setProfile(profiles);
        user.setRoleName(User.Role.SinhVien);
        profiles.setUser(user);
        profiles.setAccount(account);
        accountRepository.save(account);
    }

    @Override
    public Account findUserByUsername(String username){
        return accountRepository.findByUsername(username);
    }

    @Override
    public Account findByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    @Override
    public Page<Account> searchByKeywordAndRole(String search, Account.Role role, Pageable pageable){
        return accountRepository.searchByKeywordAndRole(search, role, pageable);
    }

    @Override
    public Page<Account> searchUserByKeywordAndRoleName(String search, Pageable pageable){
        return accountRepository.searchUserByKeywordAndRoleName(search, Account.Role.User, pageable);
    }
    @Override
    public Page<Account> findAccountByRoleAndUserRole(User.Role roleName, Pageable pageable){
        return accountRepository.findAccountByRoleAndUserRole(roleName, pageable);
    }
    // ??? wtf
    @Override
    public Page<Account> findAccountByRole(Account.Role role, Pageable pageable){
        if (role == Account.Role.User) {
            return accountRepository.findAccountByUser(role, pageable);
        }
        return accountRepository.getListAccount(role, pageable);
    }

    @Override
    public Account blockOrOpenAccount(Integer id){
        Account acc = accountRepository.findByAccountID(id);
        if (acc.isBlock()){
            acc.setBlock(Boolean.FALSE);
            accountRepository.save(acc);
            return acc;
        }
        else {
            acc.setBlock(Boolean.TRUE);
            accountRepository.save(acc);
            return acc;
        }
    }
    @Override
    public Account findAccountByID(Integer id){
        return accountRepository.findByAccountID(id);
    }

    @Transactional
    @Override
    public Account createManagerOrConsultant(Account account, String password, MultipartFile avatarFile) {
        if (accountRepository.findByUsername(account.getUsername()) != null) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }
        if (accountRepository.findByEmail(account.getEmail()) != null) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        account.setPassword(passwordEncoder.encode(password));
        account.setCreatedDate(new Date());
        Account acc = accountRepository.save(account);  // save lan 1

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarFileName = fileStorageService.storeFile(avatarFile,acc.getProfile().getAvatar(),"avatars");
            acc.getProfile().setAvatar(avatarFileName);
        }
        return accountRepository.save(acc);
    }

    @Transactional
    @Override
    public Account editManagerOrConsultant(Account account, String pass, MultipartFile avatarFile) {
        // 1️⃣ Kiểm tra trùng username / email
        if (accountRepository.existsByUsernameAndAccountIDNot(account.getUsername(), account.getAccountID())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }
        if (accountRepository.existsByEmailIgnoreCaseAndAccountIDNot(account.getEmail(), account.getAccountID())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        // 2️⃣ Lấy account cũ từ DB với profile
        Account updatedAccount = accountRepository.findByAccountID(account.getAccountID());

        Profiles existingProfile = updatedAccount.getProfile();

        // 3️⃣ Cập nhật avatar (nếu có)
        String oldAvatar = existingProfile.getAvatar();
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Xóa ảnh cũ nếu có trên cloud
            if (oldAvatar != null && oldAvatar.contains("cloudinary.com")) {
                fileStorageService.deleteFile(oldAvatar);
            }
            // Lưu ảnh mới
            String newAvatar = fileStorageService.storeFile(avatarFile, oldAvatar, "avatars");
            existingProfile.setAvatar(newAvatar);
        }

        // 4️⃣ Cập nhật các trường của profile
        Profiles newProfile = account.getProfile();
        if (newProfile != null) {
            existingProfile.setFullName(newProfile.getFullName());
            existingProfile.setPhone(newProfile.getPhone());
        }

        // 5️⃣ Cập nhật mật khẩu
        if (pass != null && !pass.isBlank()) {
            updatedAccount.setPassword(passwordEncoder.encode(pass));
        }

        // 6️⃣ Cập nhật thông tin cơ bản
        updatedAccount.setUsername(account.getUsername());
        updatedAccount.setEmail(account.getEmail());

        // Kiểm tra xem có thay đổi role không
        Account.Role oldRole = updatedAccount.getRole();
        Account.Role newRole = account.getRole();
        boolean roleChanged = oldRole != newRole;

        updatedAccount.setRole(newRole);

        // 7️⃣ Xử lý role đặc biệt - chỉ xử lý 4 role
        switch (newRole) {
            case Admin:
                if (existingProfile.getAdmin() == null) {
                    Admin admin = adminRepository.findByProfile_ProfileID(existingProfile.getProfileID());
                    if (admin == null) {
                        // Tạo mới nếu chưa có
                        admin = new Admin();
                        admin.setProfile(existingProfile);
                        admin.setSecretPin(""); // Đặt giá trị mặc định hoặc từ input
                        admin = adminRepository.save(admin);
                    }
                    existingProfile.setAdmin(admin);
                }
                // Xóa các entity con khác nếu chuyển role
                if (roleChanged) {
                    existingProfile.setManager(null);
                    existingProfile.setConsultant(null);
                    existingProfile.setUser(null);
                }
                break;

            case Manager:
                if (existingProfile.getManager() == null) {
                    Manager manager = managerRepository.findByProfile_ProfileID(existingProfile.getProfileID());
                    if (manager == null) {
                        manager = new Manager();
                        manager.setProfile(existingProfile);
                        manager.setSecretPin("");
                        manager = managerRepository.save(manager);
                    }
                    existingProfile.setManager(manager);
                }
                if (roleChanged) {
                    existingProfile.setAdmin(null);
                    existingProfile.setConsultant(null);
                    existingProfile.setUser(null);
                }
                break;

            case Consultant:
                if (existingProfile.getConsultant() == null) {
                    Consultant consultant = consultantRepository.findByProfile_ProfileID(existingProfile.getProfileID());
                    if (consultant == null) {
                        consultant = new Consultant();
                        consultant.setProfile(existingProfile);
                        consultant.setExperienceYears(0);
                        consultant = consultantRepository.save(consultant);
                    }
                    existingProfile.setConsultant(consultant);
                }
                if (roleChanged) {
                    existingProfile.setAdmin(null);
                    existingProfile.setManager(null);
                    existingProfile.setUser(null);
                }
                break;

            case User:
                User user = existingProfile.getUser();
                if (user == null) {
                    user = userRepository.findByProfile_ProfileID(existingProfile.getProfileID())
                            .orElseGet(() -> {
                                User newUser = new User();
                                newUser.setProfile(existingProfile);
                                newUser.setRoleName(User.Role.Khac); // Mặc định
                                return userRepository.save(newUser);
                            });
                    existingProfile.setUser(user);
                }

                // Cập nhật studentCode và roleName
                User inputUser = account.getProfile() != null ? account.getProfile().getUser() : null;
                if (inputUser != null) {
                    if (inputUser.getStudentCode() != null) {
                        user.setStudentCode(inputUser.getStudentCode());
                    }
                    if (inputUser.getRoleName() != null) {
                        user.setRoleName(inputUser.getRoleName());
                    }
                    userRepository.save(user);
                }

                if (roleChanged) {
                    existingProfile.setAdmin(null);
                    existingProfile.setManager(null);
                    existingProfile.setConsultant(null);
                }
                break;
        }
        profilesRepository.save(existingProfile);
        return accountRepository.save(updatedAccount);
    }
    @Override
    public void deleteAccount(Integer id) {
        try {
            accountRepository.deleteById(id);
        } catch (AppException e) {
            throw new AppException(ErrorCode.ERROR_DELETED);
        }
    }

    @Override
    public void save(Account account) {
        accountRepository.save(account);
    }
    @Override
    public void updateAccountOffline(Integer id){
        Profiles profiles=profilesRepository.findByAccountId(Long.valueOf(id));
        profiles.setOnlineAt(new Date());
        profilesRepository.save(profiles);
    }
    @Override
    public String isAccountOnline(Integer id){
        Boolean status = onlineCache.getIfPresent(id);
        if (status != null && status) {
            return "Online";
        }
        Profiles profiles = profilesRepository.findByAccountId(Long.valueOf(id));
        if (profiles == null || profiles.getOnlineAt() == null) {
            return "Offline (unknown)";
        }
        Date lastOnline = profiles.getOnlineAt();
        Date now = new Date();
        long diffInMillis = now.getTime() - lastOnline.getTime();
        long diffInMinutes = diffInMillis / (60 * 1000);
        if (diffInMinutes < 1) {
            return "Vừa mới offline";
        } else if (diffInMinutes < 60) {
            return "Offline " + diffInMinutes + " phút trước";
        } else if (diffInMinutes < 1440) {
            long hours = diffInMinutes / 60;
            return "Offline " + hours + " giờ trước";
        } else {
            long days = diffInMinutes / 1440;
            return "Offline " + days + " ngày trước";
        }
    }
    @Override
    public long countAll_User() {
        return accountRepository.countByRole(Account.Role.User);
    }

}
