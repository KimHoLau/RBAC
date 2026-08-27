package com.example.system.controller;

import com.example.system.common.Result;
import com.example.system.dto.MenuRequest;
import com.example.system.entity.Menu;
import com.example.system.security.SecurityUtils;
import com.example.system.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /** menu tree of the current user, used for dynamic routes + sidebar */
    @GetMapping("/user")
    public Result<List<Menu>> userMenus() {
        return Result.success(menuService.getUserMenus(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<Menu>> fullTree() {
        return Result.success(menuService.getFullTree());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:add')")
    public Result<Void> create(@Valid @RequestBody MenuRequest request) {
        menuService.create(request);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MenuRequest request) {
        menuService.update(id, request);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success(null);
    }
}
