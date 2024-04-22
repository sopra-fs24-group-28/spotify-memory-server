package ch.uzh.ifi.hase.soprafs24.websocket;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;

@Component
public class UserHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            String token = extractToken(request);

            if (token == null) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            attributes.put("token", token);
            return true;
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }
    }

    private String extractToken(ServerHttpRequest request) {
        String query = request.getURI().getQuery();
        if (query != null) {
            String[] queryParams = query.split("&");
            for (String param : queryParams) {
                String[] keyValue = param.split("=");
                if ("token".equals(keyValue[0]) && keyValue.length > 1) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {}
}
