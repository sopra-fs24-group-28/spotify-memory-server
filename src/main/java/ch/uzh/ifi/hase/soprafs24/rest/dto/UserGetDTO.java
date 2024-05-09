package ch.uzh.ifi.hase.soprafs24.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
public class UserGetDTO {
    private Long userId;
    private String username;
    private String imageUrl;
    private Integer totalGames;
    private Integer gamesWon;
    private Integer gamesLoss;
    private Integer gamesAborted;
    private Long setsWon;

    public UserGetDTO(PlayerDTO playerDTO, UserStatsDTO userStatsDTO) {
        userId = playerDTO.getUserId();
        username = playerDTO.getUsername();
        imageUrl = playerDTO.getImageUrl();
        totalGames = userStatsDTO.getTotalGames();
        gamesWon = userStatsDTO.getGamesWon();
        gamesLoss = userStatsDTO.getGamesLoss();
        gamesAborted = userStatsDTO.getGamesAborted();
        setsWon = userStatsDTO.getTotalSetsWon();
    }
}