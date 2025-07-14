package cl.transbank.domain.dto.request;

import lombok.Data;

@Data
public class PasswordChange {

    private String email;
    private String organization_id;
}
