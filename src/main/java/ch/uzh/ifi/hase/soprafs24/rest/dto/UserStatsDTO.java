package ch.uzh.ifi.hase.soprafs24.rest.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class UserStatsDTO {
    private Long userId;
    private Integer totalGames;
    private Integer gamesWon;
    private Integer gamesLoss;
    private Integer gamesAborted;
    private Long totalSetsWon;
}
