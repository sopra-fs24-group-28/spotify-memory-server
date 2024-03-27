package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.io.Serializable;
import ch.uzh.ifi.hase.soprafs24.entity.User;


@Entity
@Table(name = "STATISTICS")
public class Statistics implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long Index;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer gameID;

    @Column(nullable = false)
    private Boolean win;

    @Column(nullable = false)
    private Boolean loss;

    @Column(nullable = false)
    private Boolean aborted;

    @Column(nullable = false)
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
