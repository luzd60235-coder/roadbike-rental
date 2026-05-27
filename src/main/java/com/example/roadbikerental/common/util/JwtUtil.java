package com.example.roadbikerental.common.util;

import com.example.roadbikerental.common.auth.LoginUser;
import com.example.roadbikerental.common.enums.RoleType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JWT 工具类。
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-hours:24}")
    private Long expireHours;

    private Key key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(LoginUser loginUser) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusHours(expireHours);
        return Jwts.builder()
                .claim("userId", loginUser.getUserId())
                .claim("username", loginUser.getUsername())
                .claim("displayName", loginUser.getDisplayName())
                .claim("roleType", loginUser.getRoleType().name())
                .claim("storeId", loginUser.getStoreId())
                .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(expireTime.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public LoginUser parseToken(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(claims.get("userId", Number.class).longValue());
        loginUser.setUsername(claims.get("username", String.class));
        loginUser.setDisplayName(claims.get("displayName", String.class));
        loginUser.setRoleType(RoleType.valueOf(claims.get("roleType", String.class)));
        Number storeId = claims.get("storeId", Number.class);
        loginUser.setStoreId(storeId == null ? null : storeId.longValue());
        return loginUser;
    }
}
