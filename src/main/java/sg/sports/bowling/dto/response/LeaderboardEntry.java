package sg.sports.bowling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardEntry {
    private Long bowlerId;
    private String bowlerName;
    private double totalPoints;
}
