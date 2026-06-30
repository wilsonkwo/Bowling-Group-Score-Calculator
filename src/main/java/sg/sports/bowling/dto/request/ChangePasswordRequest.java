package sg.sports.bowling.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request body for changing the authenticated user's password")
public class ChangePasswordRequest {

    @NotBlank(message = "Current password must not be blank")
    @Schema(description = "The user's existing password (used to verify identity)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String currentPassword;

    @NotBlank(message = "New password must not be blank")
    @Size(min = 8, message = "New password must be at least 8 characters")
    @Schema(description = "The replacement password (minimum 8 characters)", minLength = 8, requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}
