package cl.transbank.domain.dto.request;

import java.util.Map;

import lombok.Data;

@Data
public class CreateOrEditUser {
    private String connection;
    private String user_id;
    private String email;
    private String password;
    private String username;
    private String given_name;
    private String family_name;
    private String name;
    private String picture;
    private boolean blocked;
    private boolean emailVerified;
    private boolean verifyEmail;
    private String organization_id;
    private Map<String, Object> userMetadata;
    private Map<String, Object> appMetadata;
}
