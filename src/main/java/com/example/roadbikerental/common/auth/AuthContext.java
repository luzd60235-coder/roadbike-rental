package com.example.roadbikerental.common.auth;

/**
 * 当前线程登录信息容器。
 */
public final class AuthContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser loginUser = HOLDER.get();
        return loginUser == null ? null : loginUser.getUserId();
    }

    public static Long getStoreId() {
        LoginUser loginUser = HOLDER.get();
        return loginUser == null ? null : loginUser.getStoreId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
