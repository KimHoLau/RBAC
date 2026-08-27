package com.example.system.controller;

import com.example.system.common.Result;
import com.example.system.entity.Role;
import com.example.system.service.RoleService;
import org.springframework.web.bind.annotation.GetMapping;
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
}
