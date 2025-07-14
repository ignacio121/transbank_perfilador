package cl.transbank.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;
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

    @Data
    public static class Identity {
        private String provider;
        private String connection;

        @JsonProperty("user_id")
        private String userId;
    }

    @JsonProperty("app_metadata")
    private AppMetadata app_metadata;

    @Data
    public static class AppMetadata {
        private String block_type = "";
    }

    @JsonProperty("user_metadata")
    private UserMetadata user_metadata;

    @Data
    public static class UserMetadata {
        private String address = "";
    }
}
