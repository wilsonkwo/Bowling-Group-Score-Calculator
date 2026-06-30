package sg.sports.bowling.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Credentials for obtaining a JWT token")
public class LoginRequest {

    @NotBlank(message = "Username must not be blank")
    @Schema(description = "Registered username", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Password must not be blank")
    @Schema(description = "Account password", example = "changeme", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
