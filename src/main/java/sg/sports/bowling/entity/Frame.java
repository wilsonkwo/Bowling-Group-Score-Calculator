package sg.sports.bowling.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * A single frame (1-10) for a bowler in a game.
 * ball3 is only used in the 10th frame.
 * frameScore is the score for that frame alone.
 * cumulativeScore is the running total up to and including this frame.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "frame",
       uniqueConstraints = @UniqueConstraint(columnNames = {"bowler_game_id", "frame_number"}))
public class Frame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bowler_game_id", nullable = false)
    private BowlerGame bowlerGame;

    @Column(name = "frame_number", nullable = false)
    private int frameNumber; // 1-10

    @Column(name = "ball1")
    private Integer ball1; // pins knocked down on 1st ball

    @Column(name = "ball2")
    private Integer ball2; // pins knocked down on 2nd ball (null if strike in frames 1-9)

    @Column(name = "ball3")
    private Integer ball3; // only for 10th frame bonus ball

    // Calculated score contribution of this frame (includes strike/spare bonuses)
    @Column(name = "frame_score")
    private Integer frameScore;

    // Running total after this frame
    @Column(name = "cumulative_score")
    private Integer cumulativeScore;

    // Points earned specifically for this frame (e.g. strike bonus points, spare bonus points)
    @Column(name = "frame_points")
    @Builder.Default
    private double framePoints = 0.0;

    public boolean isStrike() {
        return ball1 != null && ball1 == 10;
    }

    public boolean isSpare() {
        return !isStrike() && ball1 != null && ball2 != null && (ball1 + ball2) == 10;
    }
}
