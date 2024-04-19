package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.model.game.Card;
import ch.uzh.ifi.hase.soprafs24.model.game.CardCollection;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static ch.uzh.ifi.hase.soprafs24.constant.game.GameCategory.STANDARDSONG;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.Arrays;


public class CardCollectionTest {

    @Test
    void testConstructor() throws Exception {
        final int numOfSets = 3;
        final int numOfCardsPerSet = 3;

        try (MockedStatic<SpotifyService> mocked = mockStatic(SpotifyService.class)) {
            ArrayList<String> returnArray = new ArrayList<>();
            returnArray.add("song1");
            returnArray.add("song2");
            returnArray.add("song3");

            // Mock the behavior of SpotifyService.getPlaylistData
            when(SpotifyService.getPlaylistData("accessToken", "playlist")).thenReturn(returnArray);

            GameParameters gameParameters = new GameParameters(5,numOfSets,numOfCardsPerSet, STANDARDSONG,"playlist",1,1,10,15);
            // Create a CardCollection instance
            CardCollection cardCollection = new CardCollection(gameParameters, "accessToken");

            // Assert that cards and matchingCards have the right shape
            assertEquals(numOfSets * numOfCardsPerSet, cardCollection.getCards().size());
            assertEquals(numOfSets * numOfCardsPerSet, cardCollection.getMatchingCards().size());
            assertEquals(numOfCardsPerSet - 1, cardCollection.getMatchingCards().get(cardCollection.getCards().get(0).getCardId()).size());

            // Assert that the matching cards have the same content
            Card card1 = cardCollection.getCards().get(0);
            Card card2 = cardCollection.getCardById(cardCollection.getMatchingCards().get(card1.getCardId()).get(0));
            assertEquals(card1.getContent(), card2.getContent());
        }
    }

    @Test
    void testCheckMatch() {
        try (MockedStatic<SpotifyService> mockedSpotifyService = mockStatic(SpotifyService.class)) {
            ArrayList<String> returnArray = new ArrayList<>();
            returnArray.add("song1");
            returnArray.add("song2");

            // Mock the behavior of SpotifyService.getPlaylistData
            when(SpotifyService.getPlaylistData("accessToken", "playlist")).thenReturn(returnArray);

            GameParameters gameParameters = new GameParameters(5,2,2, STANDARDSONG,"playlist",1,1,10,15);
            // Create a CardCollection instance
            CardCollection cardCollection = new CardCollection(gameParameters, "accessToken");

            // Gather cards (two matching, one non-matching)
            Card card1 = cardCollection.getCards().get(0);
            Card card2_matching_card1 = cardCollection.getCardById(cardCollection.getMatchingCards().get(card1.getCardId()).get(0));
            Card card3_not_matching;

            if (cardCollection.getCards().get(1).getCardId() == card2_matching_card1.getCardId()) {
                card3_not_matching = cardCollection.getCards().get(2);
            } else {
                card3_not_matching = cardCollection.getCards().get(1);
            }

            // Assert that the function returns true if the cards match (or there is just one card)
            assertTrue(cardCollection.checkMatch(Arrays.asList(new Integer[]{card1.getCardId()})));
            assertTrue(cardCollection.checkMatch(Arrays.asList(new Integer[]{card2_matching_card1.getCardId()})));
            assertTrue(cardCollection.checkMatch(Arrays.asList(new Integer[]{card3_not_matching.getCardId()})));
            assertTrue(cardCollection.checkMatch(Arrays.asList(new Integer[]{card1.getCardId(), card2_matching_card1.getCardId()})));

            // Assert that the function returns false if the cards don't match
            assertFalse(cardCollection.checkMatch(Arrays.asList(new Integer[]{card1.getCardId(), card3_not_matching.getCardId()})));
            assertFalse(cardCollection.checkMatch(Arrays.asList(new Integer[]{card2_matching_card1.getCardId(), card3_not_matching.getCardId()})));
        }
    }
}