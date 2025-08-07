package cl.transbank.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Date;

@Data
public class UserResponse {

    @JsonProperty("user_id")
    private String userId;
    private String username;
    private String email;
    private String name;
    private Boolean blocked = false;
    private Boolean email_verified;
    private Date created_at;
    private Date last_login;

    private List<Identity> identities;
    private List<OrganizationResponse> organizations;

    @JsonProperty("app_metadata")
    private Map<String, Object> app_metadata;

    @JsonProperty("user_metadata")
    private Map<String, Object> user_metadata;


    @Data
    public static class Identity {
        private String provider;
        private String connection;

        @JsonProperty("user_id")
        private String userId;
    }

}
