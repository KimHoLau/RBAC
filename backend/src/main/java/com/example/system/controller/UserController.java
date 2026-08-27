package com.example.system.controller;

import com.example.system.common.PageResult;
import com.example.system.common.Result;
import com.example.system.dto.UserDTO;
import com.example.system.dto.UserUpsertRequest;
import com.example.system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<PageResult<UserDTO>> page(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String keyword) {
        return Result.success(userService.page(keyword, page, size));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public Result<Void> create(@Valid @RequestBody UserUpsertRequest request) {
        userService.create(request);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpsertRequest request) {
        userService.update(id, request);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('system:user:resetPwd')")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:assignRole')")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.success(null);
    }
}
