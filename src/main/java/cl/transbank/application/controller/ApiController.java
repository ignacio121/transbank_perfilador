package cl.transbank.application.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/debug")
    public Map<String, Object> debug(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return Map.of("jwt", "null");
        return Map.of(
            "sub", jwt.getSubject(),
            "permissions", jwt.getClaimAsStringList("permissions"),
            "aud", jwt.getAudience()
        );
    }

    @GetMapping("/public")
    public String publicEndpoint() {
        return "Este es un endpoint público.";
    }

    @GetMapping("/private")
    public String privateEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        authentication.getAuthorities().forEach(authority -> {
            System.out.println(authority.getAuthority());
        });
        return "Este es un endpoint protegido.";
    }

    @GetMapping("/permisos")
    public List<String> getPermisos() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());
    }

    @GetMapping("/token-info")
    public Map<String, Object> getTokenInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof JWT jwt) {
            try {
                JWTClaimsSet claims = jwt.getJWTClaimsSet();
                return claims.getClaims();
            } catch (Exception e) {
                return Map.of("error", "No se pudieron obtener los claims: " + e.getMessage());
            }
        }
        return Map.of(
            "principalClass", principal.getClass().getName(),
            "principalToString", principal.toString()
        );
    }
}
