package sg.sports.bowling.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Submit all frames for a bowler in a game at once.
 * Each inner list is one frame's balls, e.g.:
 *   [[10], [7,3], [5,2], ...] for frames 1-9
 *   [[10,10,10]] for a perfect 10th frame
 */
@Data
public class FrameSubmitRequest {

    @NotNull
    private Long bowlerId;

    @NotNull
    private Long gameId;

    @NotNull
    @Size(min = 1, max = 10)
    private List<List<Integer>> frames; // outer = frames, inner = balls per frame
}
