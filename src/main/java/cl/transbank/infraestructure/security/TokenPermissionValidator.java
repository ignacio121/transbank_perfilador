package cl.transbank.infraestructure.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public class TokenPermissionValidator {

    public static void requirePermission(Jwt jwt, String requiredPermission) {
        List<String> permissions = jwt.getClaimAsStringList("permissions");
        if (permissions == null || !permissions.contains(requiredPermission)) {
            throw new AccessDeniedException("Permiso requerido: " + requiredPermission);
        }
    }

    public static void requireAnyPermission(Jwt jwt, List<String> requiredPermissions) {
        List<String> permissions = jwt.getClaimAsStringList("permissions");
        if (permissions == null || requiredPermissions.stream().noneMatch(permissions::contains)) {
            throw new AccessDeniedException("Se requiere uno de los permisos: " + requiredPermissions);
        }
    }
}
