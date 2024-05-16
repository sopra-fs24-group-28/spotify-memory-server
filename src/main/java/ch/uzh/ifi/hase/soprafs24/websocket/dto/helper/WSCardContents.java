package ch.uzh.ifi.hase.soprafs24.websocket.dto.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;

@AllArgsConstructor
@Getter
public class WSCardContents {
    private ArrayList<WSCardContent> cardContents;
}
