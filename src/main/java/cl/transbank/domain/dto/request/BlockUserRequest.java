package cl.transbank.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import cl.transbank.domain.dto.response.UserResponse.AppMetadata;
import lombok.Data;

@Data
public class BlockUserRequest {

    private Boolean blocked;

    @JsonProperty("app_metadata")
    private AppMetadata app_metadata;
}
