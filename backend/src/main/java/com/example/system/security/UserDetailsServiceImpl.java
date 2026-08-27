package com.example.system.security;

import com.example.system.entity.User;
import com.example.system.entity.UserRole;
import com.example.system.repository.MenuRepository;
import com.example.system.repository.UserRepository;
import com.example.system.repository.UserRoleRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final MenuRepository menuRepository;

    public UserDetailsServiceImpl(UserRepository userRepository,
                                  UserRoleRepository userRoleRepository,
                                  MenuRepository menuRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.menuRepository = menuRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        List<Long> roleIds = userRoleRepository.findByUserId(user.getId()).stream()
                .map(UserRole::getRoleId)
                .toList();

        Set<String> perms = roleIds.isEmpty()
                ? Set.of()
                : new HashSet<>(menuRepository.findPermsByRoleIds(roleIds));

        return new LoginUser(user, perms);
    }
}
