package ch.uzh.ifi.hase.soprafs24.websocket.dto.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
public class WSScoreBoardChanges {
    Map<Long, RankDouble> scoreboard = new HashMap<>();

    public WSScoreBoardChanges(Map<Long, Long> nonRankScoreboard) {
        List<Map.Entry<Long, Long>> entries = new ArrayList<>(nonRankScoreboard.entrySet());

        entries.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        int rank = 1;

        long lastScore = entries.get(0).getValue();
        int skip = 0;

        for (Map.Entry<Long, Long> entry : entries) {
            if (entry.getValue() != lastScore) {
                rank += skip;
                skip = 1;
                lastScore = entry.getValue();
            } else {
                skip++;
            }

            RankDouble rankDouble = new RankDouble(rank, entry.getValue());
            scoreboard.put(entry.getKey(), rankDouble);
        }
    }
}

@Getter
@Setter
@AllArgsConstructor
class RankDouble {
    private Integer rank;
    private Long score;
}
