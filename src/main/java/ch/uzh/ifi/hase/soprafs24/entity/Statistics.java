package ch.uzh.ifi.hase.soprafs24.entity;

// import javax.persistence.*;
import java.io.Serializable;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;


@Document(collection = "STATISTICS")
public class Statistics implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    private Long Index;

    @Indexed(unique = true)
    private Long userId;

    private Integer gameID;

    private Boolean win;

    private Boolean loss;

    private Boolean aborted;

    private Integer setsWon;

    public Long getIndex() {
        return Index;
    }

    public void setIndex(Long index) {
        Index = index;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getGameID() {
        return gameID;
    }

    public void setGameID(Integer gameID) {
        this.gameID = gameID;
    }

    public Boolean getWin() {
        return win;
    }

    public void setWin(Boolean win) {
        this.win = win;
    }

    public Boolean getLoss() {
        return loss;
    }

    public void setLoss(Boolean loss) {
        this.loss = loss;
    }

    public Boolean getAborted() {
        return aborted;
    }

    public void setAborted(Boolean aborted) {
        this.aborted = aborted;
    }

    public Integer getSetsWon() {
        return setsWon;
    }

    public void setSetsWon(Integer setsWon) {
        this.setsWon = setsWon;
    }
}
