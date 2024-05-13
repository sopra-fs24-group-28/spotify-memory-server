package ch.uzh.ifi.hase.soprafs24.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InactiveRequestEvent extends ApplicationEvent {

    private final Integer gameId;

    public InactiveRequestEvent(Object source, Integer gameId) {
        super(source);
        this.gameId = gameId;
    }
}
