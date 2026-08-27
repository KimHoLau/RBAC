package com.example.system.controller;

import com.example.system.common.Result;
import com.example.system.entity.Role;
import com.example.system.service.RoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /** role options for the assign-roles dialog */
    @GetMapping
    public Result<List<Role>> list() {
        return Result.success(roleService.list());
    }

    /** granted menu ids, used to echo the assign-menu dialog */
    @GetMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('system:role:assignMenu')")
    public Result<List<Long>> menuIds(@PathVariable Long id) {
        return Result.success(roleService.getMenuIds(id));
    }

    @PutMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('system:role:assignMenu')")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return Result.success(null);
    }
}
