package cl.transbank.infraestructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class Auth0ManagmentClient {

    @Value("${auth0.authorized}")
    private String authorized;

    @Value("${auth0.client-id}")
    private String clientId;

    @Value("${auth0.client-secret}")
    private String clientSecret;

    @Value("${auth0.audience}")
    private String audience;

    private final RestTemplate restTemplate = new RestTemplate();

    private String token;
    private Instant expiry;

    private static final int EXPIRY_MARGIN_SECONDS = 60;

    public synchronized String getAccessToken() {
        if (token == null || Instant.now().isAfter(expiry.minusSeconds(EXPIRY_MARGIN_SECONDS))) {
            log.info("🔐 Solicitando nuevo token de Auth0...");
            fetchNewToken();
        }
        return token;
    }

    private void fetchNewToken() {
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("audience", audience);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(authorized, HttpMethod.POST, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            this.token = (String) response.getBody().get("access_token");
            Integer expiresIn = (Integer) response.getBody().get("expires_in");
            this.expiry = Instant.now().plusSeconds(expiresIn);
            log.info("✅ Token recibido, expira en {} segundos", expiresIn);
        } else {
            throw new RuntimeException("No se pudo obtener el token de Auth0");
        }
    }
}
