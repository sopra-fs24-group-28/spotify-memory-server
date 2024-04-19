package ch.uzh.ifi.hase.soprafs24.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LobbyOverviewChangesDto {
    private HashMap<Long, LobbyGameChanges> gamesHashmap = new HashMap<>();
}
