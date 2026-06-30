package sg.sports.bowling.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Submit all frames for one bowler in one game. Re-submitting replaces all previously saved frames.")
public class FrameSubmitRequest {

    @NotNull(message = "Bowler ID is required")
    @Min(value = 1, message = "Bowler ID must be a positive integer")
    @Schema(description = "ID of the bowler", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long bowlerId;

    @NotNull(message = "Game ID is required")
    @Min(value = 1, message = "Game ID must be a positive integer")
    @Schema(description = "ID of the game", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long gameId;

    @NotNull(message = "Frames list is required")
    @Size(min = 1, max = 10, message = "Must submit between 1 and 10 frames")
    @Schema(description = "List of frames; each frame is a list of ball scores. Frames 1–9: 1 ball (strike) or 2 balls. Frame 10: 2–3 balls.",
            example = "[[10],[7,3],[9,0],[10],[0,8],[8,2],[0,6],[10],[10],[10,8,1]]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private List<
            @NotNull(message = "Frame entry must not be null")
            @Size(min = 1, max = 3, message = "Each frame must have 1 to 3 balls")
            List<@NotNull(message = "Ball score must not be null") @Min(value = 0, message = "Ball score must be 0 or more") @Max(value = 10, message = "Ball score must be 10 or less") Integer>
            > frames;
}
