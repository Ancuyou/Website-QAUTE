package it.ute.QAUTE.configuration;

import it.ute.QAUTE.entity.Account;
import it.ute.QAUTE.entity.Profiles;
import it.ute.QAUTE.repository.AccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    @Bean
    ApplicationRunner applicationRunner(AccountRepository accountRepository){
        return args -> {
            if(accountRepository.findByUsername("admin") == null){
                Profiles profile = new Profiles();
                profile.setFullName("Administrator");
                profile.setPhone("0000000000");
                profile.setAvatar(null);
                Account account = new Account();
                account.setUsername("admin");
                account.setPassword(passwordEncoder.encode("admin"));
                account.setEmail("admin@gmail.com");
                account.setRole(Account.Role.Admin);
                account.setProfile(profile);
                accountRepository.save(account);
                log.warn("✅ Admin account created: username=admin, password=admin. Please change it!");
            }
            // tạo consultant
            if(accountRepository.findByUsername("consultant") == null){
                Profiles profile = new Profiles();
                profile.setFullName("Consultant");
                profile.setPhone("0000000000");
                profile.setAvatar(null);
                Account account = new Account();
                account.setUsername("consultant");
                account.setPassword(passwordEncoder.encode("consultant"));
                account.setEmail("consultant@gmail.com");
                account.setRole(Account.Role.Consultant);
                account.setProfile(profile);
                accountRepository.save(account);
                log.warn("✅ Consultant account created: username=consultant, password=consultant. Please change it!");
            }

        };
    }
}
