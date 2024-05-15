package ch.uzh.ifi.hase.soprafs24.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

@Getter
@NoArgsConstructor
public class LobbyOverviewDto {
    private HashMap<Integer, LobbyGameDto> games = new HashMap<>();
}
