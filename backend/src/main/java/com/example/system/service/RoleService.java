package com.example.system.service;

import com.example.system.entity.Role;

import java.util.List;

public interface RoleService {

    List<Role> list();

    List<Long> getMenuIds(Long roleId);

    /** 覆盖式授权：以传入集合为最终状态，非增量追加 */
    void assignMenus(Long roleId, List<Long> menuIds);
}
