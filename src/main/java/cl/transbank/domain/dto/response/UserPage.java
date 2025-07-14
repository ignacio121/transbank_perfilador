package cl.transbank.domain.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class UserPage {
    private int start;
    private int limit;
    private int length;
    private int total;
    private List<UserResponse> users;
}
