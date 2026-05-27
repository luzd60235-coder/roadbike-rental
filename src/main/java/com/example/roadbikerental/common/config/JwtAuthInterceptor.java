package com.example.roadbikerental.common.config;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.auth.AuthContext;
import com.example.roadbikerental.common.auth.LoginUser;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.exception.BusinessException;
import com.example.roadbikerental.common.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * JWT 鉴权拦截器。
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RoleRequired roleRequired = handlerMethod.getMethodAnnotation(RoleRequired.class);
        if (roleRequired == null) {
            roleRequired = handlerMethod.getBeanType().getAnnotation(RoleRequired.class);
        }
        if (roleRequired == null) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isBlank(authorization) || !authorization.startsWith("Bearer ")) {
            throw new BusinessException("未登录或登录已失效");
        }
        LoginUser loginUser = jwtUtil.parseToken(authorization.substring(7));
        if (loginUser == null) {
            throw new BusinessException("登录凭证无效");
        }
        boolean matched = Arrays.stream(roleRequired.value()).anyMatch(roleType -> roleType == loginUser.getRoleType());
        if (!matched) {
            throw new BusinessException("无权限访问当前接口");
        }
        AuthContext.set(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
