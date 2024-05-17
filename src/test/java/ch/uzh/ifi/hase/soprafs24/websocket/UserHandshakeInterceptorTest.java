package ch.uzh.ifi.hase.soprafs24.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserHandshakeInterceptorTest {

    @Mock
    private ServerHttpRequest mockRequest;
    @Mock
    private ServerHttpResponse mockResponse;
    @Mock
    private WebSocketHandler mockWsHandler;

    private UserHandshakeInterceptor interceptor;
    private Map<String, Object> attributes;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        interceptor = new UserHandshakeInterceptor();
        attributes = new HashMap<>();
    }

    @Test
    void whenTokenPresent_thenAuthorizeHandshake() throws Exception {
        when(mockRequest.getURI()).thenReturn(new URI("https://mockedURL.com?token=validToken"));

        boolean result = interceptor.beforeHandshake(mockRequest, mockResponse, mockWsHandler, attributes);

        assertTrue(result);
        assertEquals("validToken", attributes.get("token"));
    }

    @Test
    void whenTokenMissing_thenDenyHandshake() throws Exception {
        when(mockRequest.getURI()).thenReturn(new URI("https://mockedURL.com"));

        boolean result = interceptor.beforeHandshake(mockRequest, mockResponse, mockWsHandler, attributes);

        assertFalse(result);
        verify(mockResponse).setStatusCode(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void whenTokenMalformed_thenDenyHandshake() throws Exception {
        when(mockRequest.getURI()).thenReturn(new URI("https://mockedURL.com?token="));

        boolean result = interceptor.beforeHandshake(mockRequest, mockResponse, mockWsHandler, attributes);

        assertFalse(result);
        verify(mockResponse).setStatusCode(HttpStatus.UNAUTHORIZED);
    }
}

