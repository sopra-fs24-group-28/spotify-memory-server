package ch.uzh.ifi.hase.soprafs24.websocket.config;

import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

public class OverviewStompConnectEvent implements ApplicationListener<SessionConnectedEvent> {

    private final SimpMessagingTemplate messagingTemplate;

    public OverviewStompConnectEvent(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void onApplicationEvent(SessionConnectedEvent event) {
        String welcomeMessage = "OPEN";

        String userDestination = "/overview";

        messagingTemplate.convertAndSend(userDestination, welcomeMessage);
    }
}
