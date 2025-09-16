package it.ute.QAUTE.configuration;

import it.ute.QAUTE.entity.Role;
import it.ute.QAUTE.entity.User;
import it.ute.QAUTE.repository.RoleReponsitory;
import it.ute.QAUTE.repository.UserReponsitory;
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
    ApplicationRunner applicationRunner(UserReponsitory userRepository, RoleReponsitory roleReponsitory){
        return args -> {
            if(userRepository.findByUsername("admin") == null){
                Role role = roleReponsitory.findByRoleName("ADMIN")
                        .orElseGet(() -> roleReponsitory.save(
                                Role.builder()
                                        .roleName("ADMIN")
                                        .description("This is role ADMIN")
                                        .build()
                        ));
                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .email("admin@gmail.com")
                        .role(role)
                        .build();
                userRepository.save(user);
                log.warn("Admin user has been created with default password: admin, please change it");
            }
        };
    }
}
