package com.ghost.sacred_library.config;

import com.ghost.sacred_library.entity.Role;
import com.ghost.sacred_library.repository.RoleRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class RoleInitializer {
    @Bean ApplicationRunner initializeRoles(RoleRepository roles) {
        return args -> ensureRolesExist(roles);
    }
    @Transactional
    void ensureRolesExist(RoleRepository roles) {
        for (String name : new String[]{"ROLE_USER", "ROLE_ADMIN"}) {
            if (roles.findByName(name).isEmpty()) roles.save(new Role(name));
        }
    }
}
