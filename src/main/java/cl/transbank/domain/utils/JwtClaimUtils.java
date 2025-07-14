package cl.transbank.domain.utils;

import org.springframework.security.core.context.SecurityContextHolder;

import com.nimbusds.jwt.JWT;

public class JwtClaimUtils {
    public static String getUserId() {
        try {
            JWT jwt = (JWT) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return jwt.getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}

