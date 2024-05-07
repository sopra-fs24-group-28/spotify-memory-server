package ch.uzh.ifi.hase.soprafs24.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserGetDTO {
    private Long userId;
    private String username;
    private String imageUrl;
    private Integer totalGames;
    private Integer GamesWon;
    private Integer GamesLoss;
    private Integer GamesAborted;
    private Long SetsWon;

    public UserGetDTO(PlayerDTO playerDTO, UserStatsDTO userStatsDTO) {
        userId = playerDTO.getUserId();
        username = playerDTO.getUsername();
        imageUrl = playerDTO.getImageUrl();
        totalGames = userStatsDTO.getTotalGames();
        GamesWon = userStatsDTO.getGamesWon();
        GamesLoss = userStatsDTO.getGamesLoss();
        GamesAborted = userStatsDTO.getGamesAborted();
        SetsWon = userStatsDTO.getTotalSetsWon();
    }
}