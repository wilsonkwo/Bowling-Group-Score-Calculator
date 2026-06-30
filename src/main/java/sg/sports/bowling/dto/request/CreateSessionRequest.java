package sg.sports.bowling.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import sg.sports.bowling.entity.BowlingSession.TimeSlot;

import java.time.LocalDate;

@Data
@Schema(description = "Request body for creating a new bowling session")
public class CreateSessionRequest {

    @NotNull(message = "Session date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Date of the bowling session", example = "2026-07-04", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate sessionDate;

    @NotNull(message = "Time slot is required")
    @Schema(description = "Time slot: MORNING (8am–12pm), AFTERNOON (1pm–6pm), EVENING (8pm–12am)",
            allowableValues = {"MORNING", "AFTERNOON", "EVENING"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private TimeSlot timeSlot;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    @Schema(description = "Optional venue name", example = "Downtown Lanes", maxLength = 200)
    private String location;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Optional free-text notes about the session", maxLength = 1000)
    private String notes;
}
