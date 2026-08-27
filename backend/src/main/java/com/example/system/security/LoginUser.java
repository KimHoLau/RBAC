package com.example.system.security;

import com.example.system.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class LoginUser implements UserDetails {

    private final User user;
    private final Set<String> perms;

    public LoginUser(User user, Set<String> perms) {
        this.user = user;
        this.perms = perms == null ? Collections.emptySet() : perms;
    }

    public Long getUserId() {
        return user.getId();
    }

    public User getUser() {
        return user;
    }

    public Set<String> getPerms() {
        return perms;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return perms.stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() != null && user.getStatus() == 1;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
