package cl.transbank.application.service;

import cl.transbank.domain.dto.request.BlockUserRequest;
import cl.transbank.domain.dto.request.CreateOrEditUser;
import cl.transbank.domain.dto.response.OrganizationResponse;
import cl.transbank.domain.dto.response.RoleResponse;
import cl.transbank.domain.dto.response.UserPage;
import cl.transbank.domain.dto.response.UserResponse;
import cl.transbank.domain.utils.PasswordGenerator;
import cl.transbank.infraestructure.client.Auth0ManagmentClient;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${auth0.audience}")
    private String auth0managmentApi;

    private final Auth0ManagmentClient auth0Client;

    @Autowired
    private OrganizationService organizationService;

    private WebClient buildClient(String token) {
        return WebClient.builder()
                .baseUrl(auth0managmentApi)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public UserResponse getUser(String userId) {
        String token = auth0Client.getAccessToken();

        return buildClient(token)
                .get()
                .uri("/users/{user_id}", userId)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .block();
    }

    public UserResponse getUserWithDetails(String userId, String orgId) {
        UserResponse user = getUser(userId);

        List<OrganizationResponse> organizations;
        if (orgId != null && !orgId.equals("0") && !orgId.isBlank()) {
            // Solo la organización especificada
            OrganizationResponse org = getOrganizationWithRoles(userId, orgId);
            organizations = org != null ? List.of(org) : List.of();
        } else {
            // Todas las organizaciones
            organizations = getUserOrganizationsWithRoles(userId);
        }
        user.setOrganizations(organizations);

        return user;
    }

    public UserPage listarUsuarios(int page, int per_page) {
        String token = auth0Client.getAccessToken();

        return buildClient(token)
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path("/users")
                    .queryParam("include_totals", "true")
                    .queryParam("page", page)
                    .queryParam("per_page", per_page)
                    .build())
                .retrieve()
                .bodyToMono(UserPage.class)
                .block();
    }

    public void bloquearUsuario(String userId, BlockUserRequest request) {
        String token = auth0Client.getAccessToken();

        Map<String, Object> body = Map.of(
            "blocked", request.getBlocked(),
            "app_metadata", request.getApp_metadata()
        );

        buildClient(token)
                .patch()
                .uri("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public List<OrganizationResponse> getUserOrganizationsWithRoles(String userId) {
        List<OrganizationResponse> organizations = getUserOrganizations(userId);

        for (OrganizationResponse org : organizations) {
            List<RoleResponse> roles = getUserRoles(userId, org.getId());
            org.setRoles(roles);
        }

        return organizations;
    }

    public OrganizationResponse getOrganizationWithRoles(String userId, String orgId) {
        String token = auth0Client.getAccessToken();

        // Obtener la organización específica
        OrganizationResponse org = buildClient(token)
                .get()
                .uri("/organizations/{id}", orgId)
                .retrieve()
                .bodyToMono(OrganizationResponse.class)
                .block();

        if (org != null) {
            List<RoleResponse> roles = getUserRoles(userId, orgId);
            org.setRoles(roles);
        }
        return org;
    }
    public List<RoleResponse> getUserRoles(String userId, String organizationId) {
        String token = auth0Client.getAccessToken();

        return buildClient(token)
                .get()
                .uri("/organizations/{organization_id}/members/{user_id}/roles", organizationId, userId)
                .retrieve()
                .bodyToFlux(RoleResponse.class)
                .collectList()
                .block();
    }

    public List<OrganizationResponse> getUserOrganizations(String userId){
        String token = auth0Client.getAccessToken();

        return buildClient(token)
                .get()
                .uri("/users/{user_id}/organizations", userId)
                .retrieve()
                .bodyToFlux(OrganizationResponse.class)
                .collectList()
                .block();
    }

    public void createUser(CreateOrEditUser createUser) {
        String token = auth0Client.getAccessToken();

        if (createUser.getPassword() == null || createUser.getPassword().isEmpty()) {
            String generatedPassword = PasswordGenerator.generatePassword(10);
            createUser.setPassword(generatedPassword);
            System.out.println("Password generated for user: " + createUser.getEmail() + " - " + createUser.getPassword());
        }

        // Armar body para creación de usuario
        Map<String, Object> body = new HashMap<>();
        putIfNotNull(body, "connection", createUser.getConnection());
        putIfNotNull(body, "email", createUser.getEmail());
        putIfNotNull(body, "given_name", createUser.getGiven_name());
        putIfNotNull(body, "family_name", createUser.getFamily_name());
        putIfNotNull(body, "name", createUser.getName());
        putIfNotNull(body, "user_id", createUser.getUser_id());
        putIfNotNull(body, "username", createUser.getUsername());
        putIfNotNull(body, "password", createUser.getPassword());

        
        UserResponse createdUser = buildClient(token)
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .block();

        String connection = createUser.getConnection();

        String organizationId = switch (connection) {
            case "PoC-Portal-TBK-Comercios" -> Optional.ofNullable(createUser.getOrganization_id())
                                                    .filter(id -> !id.isBlank())
                                                    .orElse("org_0uabXYIr34Ak1DMK");
            case "PoC-Portal-TBK-Internos" -> "org_ssyLnaow1bWKksoC";
            default -> null;
        };

        System.out.println("User created with ID: " + createdUser.getUserId() + ", Organization ID: " + organizationId);

        if (organizationId != null) {
            organizationService.addUsersToOrganization(List.of(createdUser.getUserId()), organizationId);// 5 intentos, 1 segundo entre cada uno
        }
    }

    public void updateUser(String userId,CreateOrEditUser updateUser) {
        String token = auth0Client.getAccessToken();

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required for update.");
        }

        if (updateUser.getEmail() != null && !updateUser.getEmail().isBlank() &&
            updateUser.getUsername() != null && !updateUser.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo puedes el email o el rut, no los dos en simultáneo.");
        }

        Map<String, Object> body = new HashMap<>();
        putIfNotEmpty(body, "email", updateUser.getEmail());
        putIfNotEmpty(body, "given_name", updateUser.getGiven_name());
        putIfNotEmpty(body, "family_name", updateUser.getFamily_name());
        putIfNotEmpty(body, "name", updateUser.getName());
        putIfNotEmpty(body, "username", updateUser.getUsername());

        System.out.println("Updating user: " + userId + " with fields: " + body.keySet());

        UserResponse updatedUser = buildClient(token)
                .patch()
                .uri("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .block();

        System.out.println("User updated: " + updatedUser.getUserId());
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) map.put(key, value);
    }

    private void putIfNotEmpty(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }

}