package com.example.system.controller;

import com.example.system.common.Result;
import com.example.system.dto.AuthInfoVO;
import com.example.system.dto.LoginRequest;
import com.example.system.dto.LoginResponse;
import com.example.system.security.JwtUtil;
import com.example.system.security.LoginUser;
import com.example.system.security.SecurityUtils;
import com.example.system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        List<String> authorities = loginUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        String token = jwtUtil.generateToken(loginUser.getUserId(), loginUser.getUsername(), authorities);

        return Result.success(new LoginResponse(token, userService.getUserInfoById(loginUser.getUserId())));
    }

    @GetMapping("/info")
    public Result<AuthInfoVO> info() {
        return Result.success(userService.getAuthInfo(SecurityUtils.getCurrentUserId()));
    }
}
