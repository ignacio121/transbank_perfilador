package cl.transbank.application.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.*;

import cl.transbank.domain.dto.response.OrganizationResponse;
import cl.transbank.domain.dto.response.UserOfOrganizationResponse;
import cl.transbank.domain.dto.response.UserResponse;
import cl.transbank.infraestructure.client.Auth0ManagmentClient;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class OrganizationService {

    @Value("${auth0.audience}")
    private String auth0managmentApi;

    private final Auth0ManagmentClient auth0Client;

    @Autowired
    @Lazy
    private UserService userService;

    private WebClient buildClient(String token) {
        return WebClient.builder()
                .baseUrl(auth0managmentApi)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public List<OrganizationResponse> getOrganizationsByNameContains(String partialName) {
        List<OrganizationResponse> allOrgs = getAllOrganizations();
        return allOrgs.stream()
                .filter(org -> org.getName() != null &&
                        org.getName().toLowerCase().contains(partialName.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<OrganizationResponse> getAllOrganizations(){
        String token = auth0Client.getAccessToken();

        List<OrganizationResponse> all = new ArrayList<>();
        int page = 0;
        int perPage = 100;  
        boolean more = true;

        while (more) {
            OrganizationResponse[] orgs = buildClient(token)
                .get()
                .uri("/organizations?page={page}&per_page={perPage}", page, perPage)
                .retrieve()
                .bodyToMono(OrganizationResponse[].class)
                .block();

            if (orgs != null) {
                all.addAll(Arrays.asList(orgs));
                more = orgs.length == perPage;
            } else {
                more = false;
            }

            page++;
        }

        return all;
    }

    public List<OrganizationResponse> organizationsOfUser(String userId) {
        String token = auth0Client.getAccessToken();

        return buildClient(token)
                .get()
                .uri("/users/{id}/organizations", userId)
                .retrieve()
                .bodyToMono(OrganizationResponse[].class)
                .map(List::of)
                .block();
    }

    public List<OrganizationResponse> getOrganizationsOfUserByName(String userId, String partialName) {
        List<OrganizationResponse> userOrgs = organizationsOfUser(userId);

        return userOrgs.stream()
                .filter(org -> org.getName() != null &&
                        org.getName().toLowerCase().contains(partialName.toLowerCase()))
                .collect(Collectors.toList());
    }

    public UserOfOrganizationResponse[] listOrganizationUsers(String organization, String search, Boolean blocked) {
        UserOfOrganizationResponse[] members = getMembers(organization);
        if (members == null || members.length == 0) return new UserOfOrganizationResponse[0];

        List<UserOfOrganizationResponse> enriched = new ArrayList<>();

        for (UserOfOrganizationResponse member : members) {
            UserResponse userDetails = userService.getUser(member.getUser_id());
            if (userDetails != null) {
                member.setEmail(userDetails.getEmail());
                member.setBlocked(userDetails.getBlocked());
                member.setUsername(userDetails.getUsername());

                boolean matchesText = (search == null || search.isBlank()) || (
                        containsIgnoreCase(userDetails.getEmail(), search) ||
                        containsIgnoreCase(userDetails.getName(), search) ||
                        containsIgnoreCase(userDetails.getUsername(), search)
                );

                boolean matchesBlocked = (blocked == null || userDetails.getBlocked().equals(blocked));

                if (matchesText && matchesBlocked) {
                    enriched.add(member);
                }
            }
        }

        return enriched.toArray(new UserOfOrganizationResponse[0]);
    }

    private boolean containsIgnoreCase(String field, String search) {
        return field != null && field.toLowerCase().contains(search.toLowerCase());
    }

    public UserOfOrganizationResponse[] getMembers(String organization) {
        String token = auth0Client.getAccessToken();
        
        return buildClient(token)
                .get()
                .uri("/organizations/{id}/members", organization)
                .retrieve()
                .bodyToMono(UserOfOrganizationResponse[].class)
                .block();
    }

    public void addUsersToOrganization(List<String> userIds, String organizationId) {
        String token = auth0Client.getAccessToken();

        Map<String, Object> body = Map.of("members", userIds);

        buildClient(token)
                .post()
                .uri("/organizations/{id}/members", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
