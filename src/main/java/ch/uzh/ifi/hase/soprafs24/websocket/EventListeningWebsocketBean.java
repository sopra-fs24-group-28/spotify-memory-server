package ch.uzh.ifi.hase.soprafs24.websocket;

import ch.uzh.ifi.hase.soprafs24.websocket.dto.WSLobbyOverviewChangesDto;
import ch.uzh.ifi.hase.soprafs24.websocket.events.GameChangesEvent;
import ch.uzh.ifi.hase.soprafs24.websocket.events.LobbyOverviewChangedEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EventListeningWebsocketBean {
    private final SimpMessagingTemplate template;

    @EventListener
    public void onLobbyOverviewChangedEvent(LobbyOverviewChangedEvent event) {
        WSLobbyOverviewChangesDto overviewChangesDTO = new WSLobbyOverviewChangesDto(event.getLobbyOverviewChangesDTO().getGameMap());
        template.convertAndSend("/topic/overview", overviewChangesDTO);
    }

    @EventListener
    public void updateGameStatus(GameChangesEvent gameChangesEvent) {
        template.convertAndSend("/topic/game/" + gameChangesEvent.getGameId(), gameChangesEvent.getGameChangesDto());
    }
}
