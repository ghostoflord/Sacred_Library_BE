package com.ghost.sacred_library.service;

import com.ghost.sacred_library.entity.User;
import com.ghost.sacred_library.repository.UserRepository;
import com.ghost.sacred_library.security.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
public class LibraryUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public LibraryUserDetailsService(UserRepository users) { this.users = users; }
    @Override public UserDetails loadUserByUsername(String usernameOrEmail) {
        User user = (usernameOrEmail.contains("@") ? users.findByEmail(usernameOrEmail.toLowerCase(Locale.ROOT)) : users.findByUsername(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        return new UserPrincipal(user);
    }
}
