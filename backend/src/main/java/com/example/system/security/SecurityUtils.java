package com.example.system.security;

import com.example.system.common.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser getCurrentLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        throw new BusinessException(401, "未登录或登录已过期");
    }

    public static Long getCurrentUserId() {
        return getCurrentLoginUser().getUserId();
    }

    public static String getCurrentUsername() {
        return getCurrentLoginUser().getUsername();
    }
}
