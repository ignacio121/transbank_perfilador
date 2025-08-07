package cl.transbank.domain.dto.request;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class BlockUserRequest {

    private Boolean blocked;
    
    @JsonProperty("app_metadata")
    private Map<String, Object> app_metadata;

}
