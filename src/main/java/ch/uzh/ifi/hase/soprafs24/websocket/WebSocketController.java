package ch.uzh.ifi.hase.soprafs24.websocket;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import ch.uzh.ifi.hase.soprafs24.service.AuthService;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.IncomingCardId;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
@AllArgsConstructor
public class WebSocketController {
    private GameService gameService;
    private AuthService authService;

    @MessageMapping("/overview")
    @SendTo("/topic/overview")
    public void lobbyOverview() {
    }

    @MessageMapping("/games/{gameId}")
    @SendTo("/queue/games/{gameId}")
    public void gameOverview(@DestinationVariable Integer gameId, IncomingCardId incomingCardId, SimpMessageHeaderAccessor headerAccessor) {
        // since we avoid using spring security and implement security and the security context manually (for experience)
        // we do not use the Spring Security Principal Model, rather set a session attribute upon handshake with the websocket

        try {
            User user = authService.getUserBySessionToken((String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("token"));
            UserContextHolder.setCurrentUser(user);
            gameService.runTurn(gameId, incomingCardId.getCardId());
        } finally {
            UserContextHolder.clear();
        }

    }
}
