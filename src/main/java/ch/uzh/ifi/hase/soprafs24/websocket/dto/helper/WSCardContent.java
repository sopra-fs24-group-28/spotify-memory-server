package ch.uzh.ifi.hase.soprafs24.websocket.dto.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WSCardContent {
    private Long cardId;
    private String songId;
    private String imageUrl;
}
