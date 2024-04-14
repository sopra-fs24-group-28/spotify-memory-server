package ch.uzh.ifi.hase.soprafs24.websocket.events;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ws.LobbyOverviewChangesDTO;
import lombok.*;
import org.springframework.context.ApplicationEvent;

import java.util.List;


@Getter
@Setter
public class LobbyOverviewChangedEvent extends ApplicationEvent {
    private LobbyOverviewChangesDTO lobbyOverviewChangesDTO;

    public LobbyOverviewChangedEvent(Object source, Game game) {
        super(source);
        this.lobbyOverviewChangesDTO = new LobbyOverviewChangesDTO(game);
    }

    public LobbyOverviewChangedEvent(Object source, Integer gameId, List<User> users) {
        super(source);
        this.lobbyOverviewChangesDTO = new LobbyOverviewChangesDTO(gameId ,users);
    }

    public LobbyOverviewChangedEvent(Object source, Integer gameId, GameState newGameState) {
        super(source);
        this.lobbyOverviewChangesDTO = new LobbyOverviewChangesDTO(gameId , newGameState);
    }
}
