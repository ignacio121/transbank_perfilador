package cl.transbank.application.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.transbank.application.service.UserService;
import cl.transbank.domain.dto.request.BlockUserRequest;
import cl.transbank.domain.dto.request.CreateOrEditUser;
import cl.transbank.domain.dto.response.ApiResponse;
import cl.transbank.domain.dto.response.OrganizationResponse;
import cl.transbank.domain.dto.response.RoleResponse;
import cl.transbank.domain.dto.response.UserPage;
import cl.transbank.domain.dto.response.UserResponse;
import cl.transbank.infraestructure.security.TokenPermissionValidator;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
            @AuthenticationPrincipal Jwt jwt, 
            @PathVariable("userId") String userId,
            @RequestParam(name = "org_id", defaultValue = "0") String orgId) {
        TokenPermissionValidator.requirePermission(jwt, "read:users");
        return ResponseEntity.ok(userService.getUserWithDetails(userId, orgId));
    }

    @GetMapping
    public ResponseEntity<UserPage> obtenerUsuarios(
            @AuthenticationPrincipal Jwt jwt, 
            @RequestParam(name = "page", defaultValue = "0") int page, 
            @RequestParam(name = "per_page", defaultValue = "10") int per_page) {
        TokenPermissionValidator.requirePermission(jwt, "read:users");
        return ResponseEntity.ok(userService.listarUsuarios(page, per_page));
    }

    @GetMapping("/{userId}/organization/{organizationId}/roles")
    public ResponseEntity<List<RoleResponse>> getUserRoles(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("userId") String userId,
            @PathVariable("organizationId") String organizationId) {
        TokenPermissionValidator.requirePermission(jwt, "read:organization_member");
        return ResponseEntity.ok(userService.getUserRoles(userId, organizationId)); // Spring lo serializa a JSON automáticamente
    }

    @GetMapping("/{userId}/organizations")
    public ResponseEntity<List<OrganizationResponse>> getUserOrganizations(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("userId") String userId){
        TokenPermissionValidator.requirePermission(jwt, "read:organizations");
        return ResponseEntity.ok(userService.getUserOrganizations(userId));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse> crearUsuario(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreateOrEditUser request) {
        TokenPermissionValidator.requirePermission(jwt, "create:users");
        userService.createUser(request);
        return ResponseEntity.ok(new ApiResponse("Usuario creado con éxito"));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse> editarUsuario(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("userId") String userId,
            @RequestBody CreateOrEditUser request) {

        TokenPermissionValidator.requirePermission(jwt, "update:users");
        userService.updateUser(userId, request);
 
        return ResponseEntity.ok(new ApiResponse("Usuario editado con éxito"));
    }

    @PatchMapping("/{userId}/bloquear")
    public ResponseEntity<Void> bloquearUsuario(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("userId") String userId,
            @RequestBody BlockUserRequest request
    ) {
        TokenPermissionValidator.requirePermission(jwt, "update:users");

        userService.bloquearUsuario(userId, request);
        return ResponseEntity.noContent().build();
    }
}
