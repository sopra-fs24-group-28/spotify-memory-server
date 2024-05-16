package ch.uzh.ifi.hase.soprafs24.model.helper;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
public class Change<T> {
    private boolean changed;
    private Optional<T> value;

    public Change(boolean changed, Optional<T> value) {
        this.changed = changed;
        this.value = value;
    }

    public static <T> Change<T> of(boolean changed, Optional<T> value) {
        return new Change<>(changed, value);
    }
}