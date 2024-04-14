package ch.uzh.ifi.hase.soprafs24.websocket.events;

import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ws.LobbyOverviewChangesDTO;
import lombok.*;
import org.springframework.context.ApplicationEvent;


@Getter
@Setter
public class LobbyOverviewChangedEvent extends ApplicationEvent {
    private LobbyOverviewChangesDTO lobbyOverviewChangesDTO;

    public LobbyOverviewChangedEvent(Object source, Game game) {
        super(source);
        this.lobbyOverviewChangesDTO = new LobbyOverviewChangesDTO(game);
    }
}
