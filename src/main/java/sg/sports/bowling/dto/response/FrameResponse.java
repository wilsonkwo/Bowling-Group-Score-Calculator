package sg.sports.bowling.dto.response;

import lombok.Builder;
import lombok.Data;
import sg.sports.bowling.entity.Frame;

@Data
@Builder
public class FrameResponse {
    private int frameNumber;
    private Integer ball1;
    private Integer ball2;
    private Integer ball3;
    private Integer frameScore;
    private Integer cumulativeScore;
    private double framePoints;
    private boolean strike;
    private boolean spare;

    public static FrameResponse from(Frame f) {
        return FrameResponse.builder()
                .frameNumber(f.getFrameNumber())
                .ball1(f.getBall1())
                .ball2(f.getBall2())
                .ball3(f.getBall3())
                .frameScore(f.getFrameScore())
                .cumulativeScore(f.getCumulativeScore())
                .framePoints(f.getFramePoints())
                .strike(f.isStrike())
                .spare(f.isSpare())
                .build();
    }
}
