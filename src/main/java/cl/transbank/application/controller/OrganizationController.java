package cl.transbank.application.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.transbank.application.service.OrganizationService;
import cl.transbank.domain.dto.response.OrganizationResponse;
import cl.transbank.domain.dto.response.UserOfOrganizationResponse;
import cl.transbank.infraestructure.security.TokenPermissionValidator;

@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @GetMapping("")
    public ResponseEntity<List<OrganizationResponse>> obtenerOrganizaciones(@AuthenticationPrincipal Jwt jwt) {
        TokenPermissionValidator.requirePermission(jwt, "read:organizations");
        return ResponseEntity.ok(organizationService.organizationsOfUser(jwt.getSubject()));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<UserOfOrganizationResponse[]> getOrganizationMembers(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") String id,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "blocked", required = false) Boolean blocked
    ) {
        TokenPermissionValidator.requirePermission(jwt, "read:organization_member");

        UserOfOrganizationResponse[] result = organizationService.listOrganizationUsers(id, search, blocked);
        return ResponseEntity.ok(result);
    }
}
