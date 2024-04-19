package ch.uzh.ifi.hase.soprafs24.websocket.events;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerDTO;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.WSLobbyOverviewChangesDto;
import lombok.*;
import org.springframework.context.ApplicationEvent;

import java.util.List;


@Getter
@Setter
public class LobbyOverviewChangedEvent extends ApplicationEvent {
    private WSLobbyOverviewChangesDto lobbyOverviewChangesDTO;

    public LobbyOverviewChangedEvent(Object source, Game game) {
        super(source);
        this.lobbyOverviewChangesDTO = new WSLobbyOverviewChangesDto(game);
    }

    public LobbyOverviewChangedEvent(Object source, Integer gameId, List<PlayerDTO> users) {
        super(source);
        this.lobbyOverviewChangesDTO = new WSLobbyOverviewChangesDto(gameId ,users);
    }

    public LobbyOverviewChangedEvent(Object source, Integer gameId, GameState newGameState) {
        super(source);
        this.lobbyOverviewChangesDTO = new WSLobbyOverviewChangesDto(gameId , newGameState);
    }
}
