package ch.uzh.ifi.hase.soprafs24.entity;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "STATS")
@Getter
@Setter
public class Stats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // TODO: could be replace with a composite key
    private Long id;

    @NonNull
    private int userId;

    @NonNull
    private int gameId;

    @NonNull
    private int setsWon;

    @NonNull
    private boolean win;

    @NonNull
    private boolean loss;

    @NonNull
    private boolean aborted;
}
