package ch.uzh.ifi.hase.soprafs24.model.game;

import ch.uzh.ifi.hase.soprafs24.service.SpotifyService;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class CardCollection {
    private List<Card> cards = new ArrayList<>();
    private HashMap<Integer, List<Integer>> matchingCards = new HashMap<Integer, List<Integer>>();

    // cards are created in the constructor
    public CardCollection(GameParameters gameParameters, String accessToken) {
        ArrayList<String> songIds = SpotifyService.getPlaylistData(accessToken, gameParameters.getPlaylist());

        // outer loop over the number of sets
        for (int i=0; i<gameParameters.getNumOfSets(); i++) {

            List<Card> newCards = new ArrayList<>();
            List<Integer> newCardIds = new ArrayList<>();
            // inner loop over the number of cards per set to create cards
            for (int j=0; j<gameParameters.getNumOfCardsPerSet(); j++) {
                Card newCard = new Card(songIds.get(i));
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
    }
}
