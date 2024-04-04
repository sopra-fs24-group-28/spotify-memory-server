package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.PlayerState;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player {

    private Long userId;

    private PlayerState playerState;

}
