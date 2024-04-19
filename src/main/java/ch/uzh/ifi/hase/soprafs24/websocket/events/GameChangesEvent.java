package ch.uzh.ifi.hase.soprafs24.websocket.events;

import ch.uzh.ifi.hase.soprafs24.websocket.dto.WSGameChangesDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class GameChangesEvent extends ApplicationEvent {
    private Integer gameId;
    private WSGameChangesDto gameChangesDto;

    public GameChangesEvent(Object source) {
        super(source);
    }
}
