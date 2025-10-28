package it.ute.QAUTE.configuration;

import it.ute.QAUTE.entity.*;
import it.ute.QAUTE.repository.AccountRepository;
import it.ute.QAUTE.repository.DepartmentRepository;
import it.ute.QAUTE.repository.FieldRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;


@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Bean
    ApplicationRunner applicationRunner(AccountRepository accountRepository, DepartmentRepository departmentRepository, FieldRepository fieldRepository){
        return args -> {
            // tạo admin
            if(accountRepository.findByUsername("system")==null){
                Profiles profile = new Profiles();
                profile.setFullName("System");
                profile.setPhone("0000000000");
                profile.setAvatar(null);
                Account account = new Account();
                account.setUsername("system");
                account.setPassword(passwordEncoder.encode("system"));
                account.setEmail("system@gmail.com");
                account.setCreatedDate(new Date());
                account.setRole(Account.Role.System);
                profile.setAccount(account);
                account.setProfile(profile);
                accountRepository.save(account);
                log.warn("System account created: username=system, password=system. Please change it!");
            }
            if(accountRepository.findByUsername("admin") == null){
                Profiles profile = new Profiles();
                profile.setFullName("Administrator");
                profile.setPhone("0000000000");
                profile.setAvatar(null);
                Account account = new Account();
                account.setUsername("admin");
                account.setPassword(passwordEncoder.encode("admin"));
                account.setEmail("admin@gmail.com");
                account.setCreatedDate(new Date());
                account.setRole(Account.Role.Admin);
                Admin admin=new Admin();
                admin.setSecretPin(passwordEncoder.encode("123456"));
                admin.setProfile(profile);
                profile.setAdmin(admin);
                profile.setAccount(account);
                account.setProfile(profile);
                accountRepository.save(account);
                log.warn("Admin account created: username=admin, password=admin. Please change it!");
            }
            // tạo manager
            if(accountRepository.findByUsername("manager") == null){
                Profiles profile = new Profiles();
                profile.setFullName("Manager");
                profile.setPhone("0000000000");
                profile.setAvatar(null);
                Account account = new Account();
                account.setUsername("manager");
                account.setPassword(passwordEncoder.encode("manager"));
                account.setEmail("manager@gmail.com");
                account.setCreatedDate(new Date());
                account.setRole(Account.Role.Manager);
                Manager manager=new Manager();
                manager.setSecretPin(passwordEncoder.encode("123456"));
                manager.setProfile(profile);
                profile.setManager(manager);
                profile.setAccount(account);
                account.setProfile(profile);
                accountRepository.save(account);
                log.warn("Manager account created: username=manager, password=manager. Please change it!");
            }
            // tạo consultant
            if(accountRepository.findByUsername("consultant1") == null){
                List<Department> departments = departmentRepository.findAll();
                Random random = new Random();
                for (int i=0;i<20;i++) {
                    Profiles profile = new Profiles();
                    profile.setFullName("Consultant"+i);
                    profile.setPhone("000000000"+i);
                    profile.setAvatar(null);
                    Account account = new Account();
                    account.setUsername("consultant"+i);
                    account.setPassword(passwordEncoder.encode("consultant"+i));
                    account.setEmail("consultant"+i+"@gmail.com");
                    account.setCreatedDate(new Date());
                    account.setRole(Account.Role.Consultant);
                    Consultant consultant = new Consultant();
                    consultant.setExperienceYears(random.nextInt(5) + 1);
                    consultant.setProfile(profile);
                    Department randomDept = departments.get(random.nextInt(departments.size()));
                    consultant.setDepartment(randomDept);
                    profile.setConsultant(consultant);
                    profile.setAccount(account);
                    account.setProfile(profile);
                    accountRepository.save(account);
                    log.warn("✅ Consultant account created: username={}, password={}", account.getUsername(), "consultant" + i);
                }
            }
            if(accountRepository.findByUsername("user1") == null) {
                for (int i=0;i<10;i++) {
                    Profiles profile = new Profiles();
                    profile.setFullName("User"+i);
                    profile.setPhone("000000000"+i);
                    profile.setAvatar(null);
                    Account account = new Account();
                    account.setUsername("user"+i);
                    account.setPassword(passwordEncoder.encode("user"+i));
                    account.setEmail("user"+i+"@gmail.com");
                    account.setRole(Account.Role.User);
                    account.setCreatedDate(new Date());
                    account.setProfile(profile);
                    User user = new User();
                    user.setStudentCode("2311025"+i);
                    user.setProfile(profile);
                    //user.setRoleName("Sinh Viên");
                    user.setRoleName(User.Role.SinhVien);
                    profile.setUser(user);
                    profile.setAccount(account);
                    accountRepository.save(account);
                    log.warn("✅ User account created: username={}, password={}", account.getUsername(), "user" + i);
                }
            }
        };
    }
}
