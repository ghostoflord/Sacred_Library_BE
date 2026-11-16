package com.ghost.sacred_library.config;

import com.ghost.sacred_library.entity.Role;
import com.ghost.sacred_library.entity.User;
import com.ghost.sacred_library.repository.RoleRepository;
import com.ghost.sacred_library.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println(">>> START INIT DATABASE");
        long countRoles = this.roleRepository.count();
        long countUsers = this.userRepository.count();

        if (countRoles == 0) {
            this.roleRepository.save(new Role("ROLE_USER"));
            this.roleRepository.save(new Role("ROLE_ADMIN"));
        }

        if (countUsers == 0) {
            User adminUser = new User("admin", "admin@gmail.com", this.passwordEncoder.encode("123456"));
            Role adminRole = this.roleRepository.findByName("ROLE_ADMIN").orElse(null);
            if (adminRole != null) {
                adminUser.addRole(adminRole);
            }
            this.userRepository.save(adminUser);
        }

        if (countRoles > 0 && countUsers > 0) {
            System.out.println(">>> SKIP INIT DATABASE ~ ALREADY HAVE DATA...");
        } else {
            System.out.println(">>> END INIT DATABASE");
        }
    }
}
