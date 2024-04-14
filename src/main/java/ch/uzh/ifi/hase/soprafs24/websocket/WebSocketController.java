package ch.uzh.ifi.hase.soprafs24.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    @MessageMapping("/overview")
    @SendTo("/topic/overview")
    public void lobbyOverview() {
    }
}
