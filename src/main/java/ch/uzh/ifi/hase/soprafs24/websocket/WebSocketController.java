package ch.uzh.ifi.hase.soprafs24.websocket;

import ch.uzh.ifi.hase.soprafs24.repository.inMemory.InMemoryGameRepository;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.LobbyGameChanges;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.LobbyOverviewChangesDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    private InMemoryGameRepository inMemoryGameRepository;

    @MessageMapping("/overview")
    @SendTo("/topic/overview")
    public LobbyOverviewChangesDto lobbyOverview() {
        LobbyOverviewChangesDto lobbyOverviewChangesDto = new LobbyOverviewChangesDto();
        LobbyGameChanges gameChangesDto = new LobbyGameChanges();
        lobbyOverviewChangesDto.getGamesHashmap().put(1L, gameChangesDto);
        return lobbyOverviewChangesDto;
    }
}
