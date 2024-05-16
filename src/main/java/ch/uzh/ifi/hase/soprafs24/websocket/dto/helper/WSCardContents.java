package ch.uzh.ifi.hase.soprafs24.websocket.dto.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Objects;

@AllArgsConstructor
@Getter
@Builder
public class WSCardContents {
    private List<Object[]> cardContents;

    public void addCardContent(int id, String url, String code) {
        cardContents.add(new Object[]{id, url, code});
    }
}
