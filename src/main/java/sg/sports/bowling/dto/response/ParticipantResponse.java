package sg.sports.bowling.dto.response;

import lombok.Builder;
import lombok.Data;
import sg.sports.bowling.entity.BowlerGame;

import java.util.List;

@Data
@Builder
public class ParticipantResponse {
    private Long bowlerId;
    private String bowlerName;
    private Integer totalScore;
    private double gamePoints;
    private BowlerGame.GameResult result;
    private List<FrameResponse> frames;
}
