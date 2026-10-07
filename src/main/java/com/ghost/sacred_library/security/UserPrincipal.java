package com.ghost.sacred_library.security;

import com.ghost.sacred_library.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

public final class UserPrincipal implements UserDetails {
    private final User user;
    public UserPrincipal(User user) { this.user = user; }
    public Long getId() { return user.getId(); }
    public User getUser() { return user; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).toList();
    }
    @Override public String getPassword() { return user.getPassword(); }
    @Override public String getUsername() { return user.getUsername(); }
    @Override public boolean isEnabled() { return user.isEnabled(); }
}
