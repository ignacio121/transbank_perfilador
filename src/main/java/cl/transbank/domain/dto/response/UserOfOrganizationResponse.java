package cl.transbank.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UserOfOrganizationResponse {

    @JsonProperty("user_id")
    private String user_id;
    private String email;
    private String name;
    private String picture;
    private Boolean blocked = false;
    private String username;
}
