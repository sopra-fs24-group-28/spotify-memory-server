package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.game.CardState;
import ch.uzh.ifi.hase.soprafs24.constant.game.GameCategory;
import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.constant.user.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Stats;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameConstant;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.model.game.Turn;
import ch.uzh.ifi.hase.soprafs24.model.game.Card;
import ch.uzh.ifi.hase.soprafs24.model.game.CardCollection;
import ch.uzh.ifi.hase.soprafs24.repository.StatsRepository;
import ch.uzh.ifi.hase.soprafs24.repository.inMemory.InMemoryGameRepository;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.WSGameChangesDto;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.helper.WSCardContent;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.helper.WSCardsStates;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.helper.WSGameChanges;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.helper.WSScoreBoardChanges;
import ch.uzh.ifi.hase.soprafs24.websocket.events.GameChangesEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Transactional
@AllArgsConstructor
public class GameService {

    private final UserService userService;
    private InMemoryGameRepository inMemoryGameRepository;
    private ApplicationEventPublisher eventPublisher;
    private StatsRepository statsRepository;



    public Game createGame(GameParameters gameParameters) {
         User host = UserContextHolder.getCurrentUser();
         if (host.getState().equals(UserStatus.INGAME)) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already in a game");
         }
         Game newGame = new Game(gameParameters, host);
         addPlayerToGame(newGame, host);
         setPlaylistNameAndURL(newGame);
         return inMemoryGameRepository.save(newGame);
     }

    public Game startGame(Integer gameId) {
        Game currentGame = inMemoryGameRepository.findById(gameId);
        User host = UserContextHolder.getCurrentUser();
        Long hostId = host.getUserId();
        if (currentGame.getPlayers().size() >= GameConstant.getMinPlayers() && Objects.equals(currentGame.getHostId(), hostId)){
            currentGame.setGameState(GameState.ONPLAY);

            randomizePlayersIndex(currentGame);
            createCardCollection(currentGame);
            createScoreBoard(currentGame);
            currentGame.setMatchCount(0);
            initiateNewTurn(currentGame, false);
            return inMemoryGameRepository.save(currentGame);
        } else {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to start the game.");
        }
    }

    private void randomizePlayersIndex(Game currentGame){
        List<User> players = currentGame.getPlayers();
        Collections.shuffle(players); // Set players List to random order.
        currentGame.setPlayers(players);
        inMemoryGameRepository.save(currentGame);
    }

    private void createScoreBoard(Game currentGame){
        List<User> players = currentGame.getPlayers();
        HashMap<Long, Long> scoreBoard = currentGame.getScoreBoard();

        for (User player: players) {
            Long playerId = player.getUserId();
            scoreBoard.put(playerId, 0L);
        }
        currentGame.setScoreBoard(scoreBoard);
    }

    private void initiateNewTurn(Game currentGame, boolean gameStreak){
        // TODO: gameStreak added for same player keep playing after matching cards & later powerups
        List<User> players = currentGame.getPlayers();

        int activePlayerIndex;

        if (currentGame.getActivePlayer() == null) {
            activePlayerIndex = 0;
        } else {
            activePlayerIndex = players.indexOf(userService.findUserByUserId(currentGame.getActivePlayer()));
            if (!gameStreak) {
                activePlayerIndex++;
                if (activePlayerIndex == players.size()) {
                    activePlayerIndex = 0;
                }
            }
        }

        Long activePlayerId = players.get(activePlayerIndex).getUserId();
        currentGame.setActivePlayer(activePlayerId);
        Turn turn = new Turn(activePlayerId);
        currentGame.getHistory().add(turn);
        inMemoryGameRepository.save(currentGame);
    }

    private void createCardCollection(Game currentGame){
        User host = UserContextHolder.getCurrentUser();
        CardCollection CardCollection = new CardCollection(currentGame.getGameParameters(), host.getSpotifyJWT().getAccessToken());
        currentGame.setCardCollection(CardCollection);
        inMemoryGameRepository.save(currentGame);
    }

     public List<Game> getGames() {
         return inMemoryGameRepository.findAll();
     }

     public Game getGameById(Integer gameId) {return inMemoryGameRepository.findById(gameId);}

     public List<User> addPlayerToGame(Integer gameId) {
         User newUser = UserContextHolder.getCurrentUser();
         Game game = inMemoryGameRepository.findById(gameId);
         if (game.getGameState() == GameState.OPEN && game.getPlayers().size() < game.getGameParameters().getPlayerLimit()){
             return addPlayerToGame(game, newUser);
         } else {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to join the game.");
         }
     }

     public List<User> removePlayerFromGame(Integer gameId) {
         User userToRemove = UserContextHolder.getCurrentUser();
         Game game = inMemoryGameRepository.findById(gameId);
         userService.setPlayerState(userToRemove, UserStatus.ONLINE);
         if (game.getHostId().equals(userToRemove.getUserId())) {
             for (User user: game.getPlayers()) {
                 userService.setPlayerState(user, UserStatus.ONLINE);
             }
             inMemoryGameRepository.deleteById(gameId);
             return null;
         } else {
             game.getPlayers().removeIf(u -> u.getUserId().equals(userToRemove.getUserId()));
             return inMemoryGameRepository.save(game).getPlayers();
         }
     }

    private List<User> addPlayerToGame(Game game, User user) {
        if (game.getGameState().equals(GameState.OPEN)) {
            userService.setPlayerState(user, UserStatus.INGAME);
            game.getPlayers().add(user);
            return inMemoryGameRepository.save(game).getPlayers();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game is not open");
        }
    }

    private Game setPlaylistNameAndURL(Game game) {
         HashMap<String,String> playlistMetadata = SpotifyService.getPlaylistMetadata(
                 UserContextHolder.getCurrentUser().getSpotifyJWT().getAccessToken(),
                 game.getGameParameters().getPlaylist().getPlaylistId()
         );

        game.getGameParameters().getPlaylist().setPlaylistName(playlistMetadata.get("playlist_name"));
        game.getGameParameters().getPlaylist().setPlaylistImageUrl(playlistMetadata.get("image_url"));

        return inMemoryGameRepository.save(game);
    }

    public void runTurn(Integer gameId, Integer cardId) throws InterruptedException {
        Game currentGame = inMemoryGameRepository.findById(gameId);

        if (checkActivePlayer(currentGame) && checkActiveCard(currentGame, cardId)){
            runActiveTurn(currentGame, cardId);
            inMemoryGameRepository.save(currentGame);
        }
        /*
        Map<Integer, CardState> integerCardStateMap = Map.of(cardId, CardState.EXCLUDED);

        WSGameChangesDto wsGameChangesDto = WSGameChangesDto.builder()
                .gameChangesDto(
                        WSGameChanges.builder().gameState(GameState.ONPLAY).build())
                .cardContent(
                        new WSCardContent(1, "sadfgsdfg", "url"))
                .cardsStates(
                        new WSCardsStates(integerCardStateMap)).build();

        eventPublisher.publishEvent(new GameChangesEvent(this, gameId, wsGameChangesDto));
         */
    }

    private boolean checkActivePlayer(Game currentGame){
        User currentPlayer = UserContextHolder.getCurrentUser();
        return Objects.equals(currentGame.getActivePlayer(), currentPlayer.getUserId());
    }

    private boolean checkActiveCard(Game currentGame, Integer cardId){
        Card currentCard = currentGame.getCardCollection().getCardById(cardId);
        return Objects.equals(currentCard.getCardState(), CardState.FACEDOWN);
    }

    private void runActiveTurn(Game currentGame, Integer cardId) throws InterruptedException {
        List<Turn> history = currentGame.getHistory();
        Turn currentTurn = history.get(history.size()-1);

        currentTurn.getPicks().add(cardId);
        Card card = currentGame.getCardCollection().getCardById(cardId);
        card.setCardState(CardState.FACEUP);

        // if gamecategory == standardsong, set song on all players' devices
        if (currentGame.getGameParameters().getGameCategory() == GameCategory.STANDARDSONG) {
            for (User player : currentGame.getPlayers()) {
                User fetchedPlayer = userService.findUserByUserId(player.getUserId());
                SpotifyService.setSong(
                        fetchedPlayer.getSpotifyJWT().getAccessToken(),
                        fetchedPlayer.getSpotifyDeviceId(),
                        card.getSongId());
            }
        }

        publishCardContents(currentGame, card);
        Thread.sleep(GameConstant.getViewSleep());
        // set sleep for all card flips.

        currentGame = handleMatch(currentGame, currentTurn);

        if (checkFinished(currentGame)){
            finishGame(currentGame);
            publishGamefinished(currentGame);
            Thread.sleep(GameConstant.getFinishSleep());

            resetGame(currentGame); // TODO: create a separate request on frontend request
        } else {
            publishOnPlayState(currentGame);
        }
    }

    private void publishCardContents(Game currentGame, Card card){

        WSGameChangesDto wsGameChangesDto = WSGameChangesDto.builder()
                .cardContent(new WSCardContent(card.getCardId(), card.getSongId(), card.getImageUrl()))
                .cardsStates(new WSCardsStates(mapCardsState(currentGame.getCardCollection())))
                .build();

        eventPublisher.publishEvent(new GameChangesEvent(this, currentGame.getGameId(), wsGameChangesDto));
    }

    //TODO: should set this functions in DTOs or elsewhere?
    private Map<Integer, CardState> mapCardsState(CardCollection cardCollection) {
        List<Card> cards = cardCollection.getCards();
        Map<Integer, CardState> cardsState = null;

        for (Card card : cards) {
            cardsState.put(card.getCardId(), card.getCardState());
        }
        return cardsState;
    }

    private Game handleMatch(Game currentGame, Turn currentTurn){

        if (checkMatch(currentGame, currentTurn)){
            if (isCompleteSet(currentGame, currentTurn)){
                winPoints(currentGame, currentTurn);
                initiateNewTurn(currentGame, true);
            }
        } else {
            setCardsFaceDown(currentGame, currentTurn);
            initiateNewTurn(currentGame, false);
        }

        return inMemoryGameRepository.save(currentGame);
    }

    private boolean checkMatch(Game currentGame, Turn currentTurn){
        return currentGame.getCardCollection().checkMatch(currentTurn.getPicks());
    }

    private boolean isCompleteSet(Game currentGame, Turn currentTurn){
        return currentTurn.getPicks().size() == currentGame.getGameParameters().getNumOfCardsPerSet();
    }

    private void winPoints(Game currentGame, Turn currentTurn) {
        Long userId = UserContextHolder.getCurrentUser().getUserId();
        Long score = currentGame.getScoreBoard().get(userId);
        currentGame.getScoreBoard().put(userId, score + currentGame.getGameParameters().getNumOfCardsPerSet());
        setCardsExcluded(currentGame, currentTurn);
        addMatchCount(currentGame);
        inMemoryGameRepository.save(currentGame);
    }

    private void setCardsFaceDown(Game currentGame, Turn currentTurn){
        CardCollection cardCollection = currentGame.getCardCollection();

        for (int cardId : currentTurn.getPicks()){
            cardCollection.getCardById(cardId).setCardState(CardState.FACEDOWN);
        }
        inMemoryGameRepository.save(currentGame);
    }

    private void setCardsExcluded(Game currentGame, Turn currentTurn){
        CardCollection cardCollection = currentGame.getCardCollection();

        for (int cardId : currentTurn.getPicks()){
            cardCollection.getCardById(cardId).setCardState(CardState.EXCLUDED);
        }
        inMemoryGameRepository.save(currentGame);
    }

    private void addMatchCount(Game currentGame){
        Integer matchCount = currentGame.getMatchCount();
        matchCount++;

        currentGame.setMatchCount(matchCount);
        inMemoryGameRepository.save(currentGame);
    }

    private void publishOnPlayState(Game currentGame){

        WSGameChangesDto wsGameChangesDto = WSGameChangesDto.builder()
                .gameChangesDto(WSGameChanges.builder()
                        .activePlayer(currentGame.getActivePlayer()).build())
                .cardsStates(
                        new WSCardsStates(mapCardsState(currentGame.getCardCollection())))
                .scoreBoard(
                        new WSScoreBoardChanges()) // TODO: set WSScoreBoardChanges()
                .build();

        eventPublisher.publishEvent(new GameChangesEvent(this, currentGame.getGameId(), wsGameChangesDto));
    }

    private boolean checkFinished(Game currentGame){
        return (currentGame.getMatchCount() == currentGame.getGameParameters().getNumOfSets());
    }

    private void finishGame(Game currentGame){
        currentGame.setGameState(GameState.FINISHED);
        recordGameStatistics(currentGame);
        resetGame(currentGame);
        inMemoryGameRepository.save(currentGame);
    }

    private void recordGameStatistics(Game currentGame){
        Random random = new Random();
        Integer gameId = random.nextInt(2147483647);
        List<User> players = currentGame.getPlayers();

        for (User player: players){
            Stats stats = new Stats();
            stats.setUserId(player.getUserId());
            stats.setGameId(gameId);
            stats.setSetsWon(currentGame.getScoreBoard().get(player.getUserId()));
            // TODO: how to get win, loss & aborted
            statsRepository.saveAndFlush(stats);
        }

    }

    private void resetGame(Game currentGame){
        currentGame.setGameState(GameState.OPEN);
        currentGame.setHistory(null);
        currentGame.setActivePlayer(null);
        currentGame.setScoreBoard(null);
        currentGame.setMatchCount(null);
        currentGame.setCardCollection(null);

        inMemoryGameRepository.save(currentGame);
    }

    private void publishGamefinished(Game currentGame){

        WSGameChangesDto wsGameChangesDto = WSGameChangesDto.builder()
                .gameChangesDto(WSGameChanges.builder()
                        .gameState(currentGame.getGameState()).build())
                .scoreBoard(
                        new WSScoreBoardChanges()) // TODO: set WSScoreBoardChanges()
                .build();

        eventPublisher.publishEvent(new GameChangesEvent(this, currentGame.getGameId(), wsGameChangesDto));
    }

}
