package ch.uzh.ifi.hase.soprafs24.model.game;

import ch.uzh.ifi.hase.soprafs24.constant.game.CardState;
import ch.uzh.ifi.hase.soprafs24.constant.game.GameCategory;
import ch.uzh.ifi.hase.soprafs24.service.SpotifyService;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
public class CardCollection {
    private List<Card> cards = new ArrayList<>();
    private HashMap<Integer, List<Integer>> matchingCards = new HashMap<>();

    // cards are created in the constructor
    public CardCollection(GameParameters gameParameters, String accessToken) {
        ArrayList<ArrayList<String>> songs = SpotifyService.getPlaylistData(accessToken, gameParameters.getPlaylist().getPlaylistId(), gameParameters.getNumOfSets());

        // outer loop over the number of sets
        for (int i=0; i<gameParameters.getNumOfSets(); i++) {

            List<Card> newCards = new ArrayList<>();
            List<Integer> newCardIds = new ArrayList<>();
            // inner loop over the number of cards per set to create cards
            for (int j=0; j<gameParameters.getNumOfCardsPerSet(); j++) {
                Card newCard = new Card(null, null);

                // create new card (content depends on GameCategory)
                if (gameParameters.getGameCategory() == GameCategory.STANDARDSONG) {
                    newCard = new Card(songs.get(i).get(0), null);
                } else if (gameParameters.getGameCategory() == GameCategory.STANDARDALBUMCOVER) {
                    newCard = new Card(null, songs.get(i).get(1));
                }

                newCards.add(newCard);
                newCardIds.add(newCard.getCardId());
            }

            // inner loop to populate cards and matchingCards
            for (int j=0; j<gameParameters.getNumOfCardsPerSet(); j++) {
                this.cards.add(newCards.get(j));
                // create new array for the matching cards
                List<Integer> matching = new ArrayList<>();
                for (int m=0; m<gameParameters.getNumOfCardsPerSet(); m++) {
                    if (m!=j) {
                        matching.add(newCardIds.get(m));
                    }
                }
                this.matchingCards.put(newCardIds.get(j), matching);
            }
        }
        Collections.shuffle(this.cards);
    }

    public Boolean checkMatch(List<Integer> picks) {
        boolean result = true;
        if (picks.size() >= 2) {
            // get the list of matching cards for the first pick
            List<Integer> matching = this.matchingCards.get(picks.get(0));
            for (int i = 1; i < picks.size(); i++) {
                if (!matching.contains(picks.get(i))) {
                    result = false;
                    break;
                }
            }
        }
        // return true if all picks match or less than 2 picks have been recorded
        return result;
    }

    public HashMap<Integer, CardState> getAllCardStates() {
        // function that returns a hashmap of cardId, CardState pairs
        HashMap<Integer, CardState> allCardStates = new HashMap<Integer, CardState>();
        for (Card card: this.cards) {
            allCardStates.put(card.getCardId(), card.getCardState());
        }
        return allCardStates;
    }

    public Card getCardById(Integer cardId) {
        Card result = null;
        for (Card card : this.cards) {
            if (card.getCardId() == cardId) {
                result = card;
                break;
            }
        }
        return result;
    }
}
