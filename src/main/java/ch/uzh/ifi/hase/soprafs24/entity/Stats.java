package ch.uzh.ifi.hase.soprafs24.entity;

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
    private Long userId; // TODO: connect to Users as 1:N or N:1

    @NonNull
    private Integer gameId;

    @NonNull
    private Long setsWon;

    @NonNull
    private Boolean win;

    @NonNull
    private Boolean loss;

    @NonNull
    private Boolean aborted;
}
