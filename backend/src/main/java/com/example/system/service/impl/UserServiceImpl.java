package com.example.system.service.impl;

import com.example.system.common.BusinessException;
import com.example.system.common.PageResult;
import com.example.system.dto.AuthInfoVO;
import com.example.system.dto.UserDTO;
import com.example.system.dto.UserUpsertRequest;
import com.example.system.entity.Role;
import com.example.system.entity.User;
import com.example.system.entity.UserRole;
import com.example.system.repository.MenuRepository;
import com.example.system.repository.RoleRepository;
import com.example.system.repository.UserRepository;
import com.example.system.repository.UserRoleRepository;
import com.example.system.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_RESET_PASSWORD = "123456";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           UserRoleRepository userRoleRepository,
                           RoleRepository roleRepository,
                           MenuRepository menuRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResult<UserDTO> page(String keyword, int page, int size) {
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : "";
        Page<User> result = userRepository.searchByKeyword(
                kw,
                PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "id")));
        List<UserDTO> list = result.getContent().stream().map(this::toDTO).toList();
        return PageResult.of(list, result.getTotalElements());
    }

    @Override
    public UserDTO getUserInfoById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(400, "用户不存在"));
        return toDTO(user);
    }

    @Override
    public AuthInfoVO getAuthInfo(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(400, "用户不存在"));

        List<Long> roleIds = userRoleRepository.findByUserId(id).stream()
                .map(UserRole::getRoleId)
                .toList();

        List<String> roleCodes = roleIds.isEmpty()
                ? List.of()
                : roleRepository.findAllById(roleIds).stream().map(Role::getRoleCode).toList();

        Set<String> perms = roleIds.isEmpty()
                ? Set.of()
                : new LinkedHashSet<>(menuRepository.findPermsByRoleIds(roleIds));

        AuthInfoVO vo = new AuthInfoVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRoles(roleCodes);
        vo.setPerms(List.copyOf(perms));
        return vo;
    }

    @Override
    @Transactional
    public void create(UserUpsertRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(400, "用户名已存在");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new BusinessException(400, "密码不能为空");
        }
        User user = new User();
        applyMutableFields(user, request);
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void update(Long id, UserUpsertRequest request) {
        User user = requireUser(id);
        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(400, "用户名已存在");
        }
        applyMutableFields(user, request);
        user.setUsername(request.getUsername());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireUser(id);
        userRoleRepository.deleteByUserId(id);
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void resetPassword(Long id) {
        User user = requireUser(id);
        user.setPassword(passwordEncoder.encode(DEFAULT_RESET_PASSWORD));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void assignRoles(Long id, List<Long> roleIds) {
        requireUser(id);
        if (roleIds != null && !roleIds.isEmpty()) {
            long existing = roleRepository.findAllById(new HashSet<>(roleIds)).size();
            if (existing < new HashSet<>(roleIds).size()) {
                throw new BusinessException(400, "存在无效的角色");
            }
        }
        userRoleRepository.deleteByUserId(id);
        if (roleIds != null) {
            roleIds.stream()
                    .distinct()
                    .map(roleId -> new UserRole(id, roleId))
                    .forEach(userRoleRepository::save);
        }
    }

    private User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(400, "用户不存在"));
    }

    private void applyMutableFields(User user, UserUpsertRequest request) {
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setCreateTime(user.getCreateTime());
        dto.setRoleIds(userRoleRepository.findByUserId(user.getId()).stream()
                .map(UserRole::getRoleId)
                .toList());
        return dto;
    }
}
