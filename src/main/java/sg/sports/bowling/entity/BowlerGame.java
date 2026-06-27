package sg.sports.bowling.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a bowler's participation in a single game.
 * Holds their total score and the points earned (from winning/losing).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bowler_game",
       uniqueConstraints = @UniqueConstraint(columnNames = {"bowler_id", "game_id"}))
public class BowlerGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bowler_id", nullable = false)
    private Bowler bowler;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    // Computed from frames; stored for fast querying
    @Column(name = "total_score")
    private Integer totalScore;

    // WIN, LOSS, DRAW — set once all bowlers' frames are entered
    @Enumerated(EnumType.STRING)
    @Column(name = "result")
    private GameResult result;

    // Points earned for this game (win/loss points)
    @Column(name = "game_points")
    @Builder.Default
    private double gamePoints = 0.0;

    public enum GameResult {
        WIN, LOSS, DRAW
    }
}
