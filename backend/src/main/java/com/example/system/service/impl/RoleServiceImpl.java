package com.example.system.service.impl;

import com.example.system.common.BusinessException;
import com.example.system.entity.Role;
import com.example.system.entity.RoleMenu;
import com.example.system.repository.MenuRepository;
import com.example.system.repository.RoleMenuRepository;
import com.example.system.repository.RoleRepository;
import com.example.system.service.RoleService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final MenuRepository menuRepository;

    public RoleServiceImpl(RoleRepository roleRepository,
                           RoleMenuRepository roleMenuRepository,
                           MenuRepository menuRepository) {
        this.roleRepository = roleRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.menuRepository = menuRepository;
    }

    @Override
    public List<Role> list() {
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    public List<Long> getMenuIds(Long roleId) {
        requireRole(roleId);
        return roleMenuRepository.findByRoleId(roleId).stream()
                .map(RoleMenu::getMenuId)
                .toList();
    }

    /** 全量覆盖授权：先校验，再删旧、存新（去重），语义与 UserService#assignRoles 一致 */
    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        requireRole(roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            long existing = menuRepository.findAllById(new HashSet<>(menuIds)).size();
            if (existing < new HashSet<>(menuIds).size()) {
                throw new BusinessException(400, "存在无效的菜单");
            }
        }
        roleMenuRepository.deleteByRoleId(roleId);
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        List<RoleMenu> grants = menuIds.stream()
                .distinct()
                .map(menuId -> new RoleMenu(roleId, menuId))
                .toList();
        roleMenuRepository.saveAll(grants);
    }

    private void requireRole(Long id) {
        roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(400, "角色不存在"));
    }
}
