package ch.uzh.ifi.hase.soprafs24.websocket;

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
        template.convertAndSend("/topic/overview", event);
    }
}
