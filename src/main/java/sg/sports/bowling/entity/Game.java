package sg.sports.bowling.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "game",
       uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "game_number"}))
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private BowlingSession session;

    @Column(name = "game_number", nullable = false)
    private int gameNumber; // 1, 2, 3...
}
