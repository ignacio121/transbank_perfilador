package cl.transbank.domain.dto.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OrganizationResponse {

    private String id;
    private String name;
    private String display_name;
    private String organization_type;
    private List<RoleResponse> roles;

    @JsonProperty("metadata")
    public void setMetadata(Map<String, Object> metadata) {
        if (metadata != null && metadata.containsKey("organization_type")) {
            this.organization_type = String.valueOf(metadata.get("organization_type"));
        }
    }
}
