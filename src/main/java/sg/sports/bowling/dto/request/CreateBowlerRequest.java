package sg.sports.bowling.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request body for creating a new bowler")
public class CreateBowlerRequest {

    @NotBlank(message = "Name must not be blank")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    @Schema(description = "Bowler's display name", example = "Alice", minLength = 1, maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
}
