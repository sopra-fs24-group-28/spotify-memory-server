package ch.uzh.ifi.hase.soprafs24.websocket.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class OverviewWebsocketController {

    @MessageMapping("/overview")
    @SendTo("/overview")
    public String greeting(String message) {
        return message.toUpperCase();
    }

}
