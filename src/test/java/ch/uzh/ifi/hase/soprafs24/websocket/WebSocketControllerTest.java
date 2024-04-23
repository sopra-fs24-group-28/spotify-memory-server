package ch.uzh.ifi.hase.soprafs24.websocket;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.service.AuthService;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.IncomingCardId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class WebSocketControllerTest {

/*    @Mock
    private GameService gameService;
    @Mock
    private AuthService authService;

    @InjectMocks
    private WebSocketController webSocketController;

    private SimpMessageHeaderAccessor headerAccessor;
    private IncomingCardId incomingCardId;
    private User testUser;

    @BeforeEach
    public void setup() {
        openMocks(this);
        headerAccessor = SimpMessageHeaderAccessor.create();
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("token", "testToken");
        headerAccessor.setSessionAttributes(sessionAttributes);

        incomingCardId = new IncomingCardId();
        incomingCardId.setCardId(1);

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setSessionToken("testToken");
    }

    public void testGameOverview() throws Exception {
        when(authService.getUserBySessionToken("testToken")).thenReturn(testUser);

        webSocketController.gameOverview(1, incomingCardId, headerAccessor);

        verify(authService).getUserBySessionToken("testToken");
        verify(gameService).runTurn(1, incomingCardId.getCardId());
    }*/

}

