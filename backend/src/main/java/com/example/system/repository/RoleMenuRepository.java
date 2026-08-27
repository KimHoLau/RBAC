package com.example.system.repository;

import com.example.system.entity.RoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleMenuRepository extends JpaRepository<RoleMenu, Long> {

    List<RoleMenu> findByRoleIdIn(List<Long> roleIds);

    boolean existsByMenuId(Long menuId);

    void deleteByRoleId(Long roleId);
}
