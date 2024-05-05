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
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerDTO;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.WSGameChangesDto;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.helper.WSCardContent;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.helper.WSCardsStates;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.helper.WSGameChanges;
import ch.uzh.ifi.hase.soprafs24.websocket.events.GameChangesEvent;
import ch.uzh.ifi.hase.soprafs24.websocket.events.LobbyOverviewChangedEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.IntStream;

@Service
@Transactional
@AllArgsConstructor
public class GameService {

    private UserService userService;
    private StatsService statsService;
    private InMemoryGameRepository inMemoryGameRepository;
    private ApplicationEventPublisher eventPublisher;
    private StatsRepository statsRepository;

    public Game createGame(GameParameters gameParameters) {

        User host = UserContextHolder.getCurrentUser();
        if (host.getState().equals(UserStatus.INGAME)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already in a game");
        }
        Game newGame = new Game(gameParameters, host);
        Game game = inMemoryGameRepository.save(newGame);
        addPlayerToGame(game, host);
        int playlistLength = setPlaylistNameAndURL(game);

        // if playlist is too short, reduce number of sets
        if (game.getGameParameters().getNumOfSets() > playlistLength) {
            game.getGameParameters().setNumOfSets(playlistLength);
        }
        return inMemoryGameRepository.save(game);
     }

    public void startGame(Integer gameId) {
        Game currentGame = inMemoryGameRepository.findById(gameId);
        User host = UserContextHolder.getCurrentUser();
        Long hostId = host.getUserId();
        if (currentGame.getPlayers().size() >= GameConstant.getMinPlayers() && Objects.equals(currentGame.getHostId(), hostId)){
            currentGame.setGameState(GameState.ONPLAY);
            currentGame.setGameStatsId(setNewGameStatsId());

            randomizePlayersIndex(currentGame);
            createCardCollection(currentGame);
            createScoreBoard(currentGame);
            currentGame.setMatchCount(0);
            initiateNewTurn(currentGame, false);
            inMemoryGameRepository.save(currentGame);

            eventPublisher.publishEvent(new LobbyOverviewChangedEvent(this, gameId, currentGame.getGameState()));

            WSGameChanges wsGameChanges = WSGameChanges.builder()
                    .gameState(currentGame.getGameState())
                    .activePlayer(currentGame.getActivePlayer())
                    .build();

            WSCardsStates wsCardsStates = new WSCardsStates(currentGame.getCardCollection().getAllCardStates());

            WSGameChangesDto wsGameChangesDto = WSGameChangesDto.builder()
                    .gameChangesDto(wsGameChanges)
                    .cardsStates(wsCardsStates)
                    .build();

            eventPublisher.publishEvent(new GameChangesEvent(this, gameId, wsGameChangesDto));


        } else {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to start the game.");
        }
    }

    private Integer setNewGameStatsId(){
        // search for Stats & inMemoryGameRepository => find largest gameStatsID.
        Integer gameStatsId = Integer.max(inMemoryGameRepository.getLatestGameStatsId(), statsService.getLatestGameId());
        gameStatsId++;

        return gameStatsId;
    }

    private void randomizePlayersIndex(Game currentGame){
        List<User> players = currentGame.getPlayers();
        Collections.shuffle(players); // Set players List to random order.
        currentGame.setPlayers(players);
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
        Long activePlayer = currentGame.getActivePlayer();

        int activePlayerIndex = 0;

        if (activePlayer != null) {
            activePlayerIndex = IntStream.range(0, players.size())
                    .filter(i -> activePlayer.equals(players.get(i).getUserId()))
                    .findFirst()
                    .orElse(0);
        }

        if (!gameStreak) {
            activePlayerIndex = (activePlayerIndex + 1) % players.size();
        }
        Long activePlayerId = players.get(activePlayerIndex).getUserId();
        currentGame.setActivePlayer(activePlayerId);
        Turn turn = new Turn(activePlayerId);
        currentGame.getHistory().add(turn);
    }

    private void createCardCollection(Game currentGame){
        User host = UserContextHolder.getCurrentUser();
        CardCollection CardCollection = new CardCollection(currentGame.getGameParameters(), host.getSpotifyJWT().getAccessToken());
        currentGame.setCardCollection(CardCollection);
    }

     public List<Game> getGames() {
         List<Game> games = inMemoryGameRepository.findAll();
         List<Game> cleanedGames = new ArrayList<>();
         for (Game game: games) {
             if (!game.getGameState().equals(GameState.FINISHED)) {
                 cleanedGames.add(game);
             }
         }
         return cleanedGames;
     }

     public Game getGameById(Integer gameId) {return inMemoryGameRepository.findById(gameId);}

     public void addPlayerToGame(Integer gameId) {
         User newUser = UserContextHolder.getCurrentUser();
         Game game = inMemoryGameRepository.findById(gameId);
         List<User> users = addPlayerToGame(game, newUser);
         sendPlayersChangedWsDto(gameId, users);
     }

     public void removePlayerFromGame(Integer gameId) {
         User userToRemove = UserContextHolder.getCurrentUser();
         removePlayerHelper(gameId, userToRemove);
     }

     private void removePlayerHelper(Integer gameId, User userToRemove) {
         Game game = inMemoryGameRepository.findById(gameId);
         userService.setPlayerState(userToRemove, UserStatus.ONLINE);
         userService.setGameIdForGivenUser(userToRemove, null);
         if (game.getHostId().equals(userToRemove.getUserId())) {
             for (User user: game.getPlayers()) {
                 userService.setPlayerState(user, UserStatus.ONLINE);
                 userService.setGameIdForGivenUser(user, null);
                 // user removed while ONPLAY -> recorded as aborted
                 if (Objects.equals(game.getGameState(),GameState.ONPLAY)){
                     recordAbortedPlayer(game, user);
                 }
             }
             inMemoryGameRepository.deleteById(gameId);
             sendGameStateChangedWsDto(gameId, GameState.FINISHED);
         } else {
             game.getPlayers().removeIf(u -> u.getUserId().equals(userToRemove.getUserId()));
             List<User> users = inMemoryGameRepository.save(game).getPlayers();
             if (Objects.equals(game.getGameState(),GameState.ONPLAY)){
                 recordAbortedPlayer(game, userToRemove);
             }
             sendPlayersChangedWsDto(gameId, users);
         }
     }

    private void recordAbortedPlayer(Game game, User userToRemove) {
        Stats stats = new Stats();
        Long userId = userToRemove.getUserId();
        stats.setUserId(userId);
        stats.setGameId(game.getGameStatsId());
        stats.setSetsWon(game.getScoreBoard().get(userToRemove.getUserId()));
        stats.setWin(false);
        stats.setLoss(false);
        stats.setAborted(true);
        statsService.saveStats(stats);
    }

    private List<User> addPlayerToGame(Game game, User user) {
        if (user.getCurrentGameId() != null || user.getState().equals(UserStatus.INGAME)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already in a game." + user.getCurrentGameId());
        } else if (game.getPlayers().size() >= game.getGameParameters().getPlayerLimit()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player limit exceeded.");
        } else if (game.getGameState().equals(GameState.ONPLAY) || game.getGameState().equals(GameState.FINISHED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game is currently not open");
        } else if (game.getGameState().equals(GameState.OPEN)) {

            userService.setGameIdForGivenUser(user, game.getGameId());
            userService.setPlayerState(user, UserStatus.INGAME);

            game.getPlayers().add(user);
            return inMemoryGameRepository.save(game).getPlayers();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There has been an unexpected exception. Logout if the issue persists");
        }
    }

    private Integer setPlaylistNameAndURL(Game game) {
         HashMap<String,String> playlistMetadata = SpotifyService.getPlaylistMetadata(
                 UserContextHolder.getCurrentUser().getSpotifyJWT().getAccessToken(),
                 game.getGameParameters().getPlaylist().getPlaylistId()
         );

        game.getGameParameters().getPlaylist().setPlaylistName(playlistMetadata.get("playlist_name"));
        game.getGameParameters().getPlaylist().setPlaylistImageUrl(playlistMetadata.get("image_url"));

        return Integer.parseInt(playlistMetadata.get("playlist_length"));
    }

    public void runTurn(Integer gameId, Integer cardId) throws InterruptedException {
        Game currentGame = inMemoryGameRepository.findById(gameId);

        synchronized (currentGame) {

            if (checkActivePlayer(currentGame) && checkActiveCard(currentGame, cardId)){
                System.out.println("Running active turn");
                runActiveTurn(currentGame, cardId);
                inMemoryGameRepository.save(currentGame);
            }
        }
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
            setSongForAllPlayers(currentGame, card);
        }

        publishCardContents(currentGame, card);
        Thread.sleep(GameConstant.getViewSleep());
        // set sleep for all card flips.

        currentGame = handleMatch(currentGame, currentTurn);

        if (checkFinished(currentGame)){
            finishGame(currentGame);
            publishGamefinished(currentGame);
            Thread.sleep(GameConstant.getFinishSleep());

            removePlayerHelper(currentGame.getGameId(), userService.findUserByUserId(currentGame.getHostId()));

            //resetGame(currentGame); // TODO: create a separate request on frontend request
        } else {
        publishOnPlayState(currentGame);
        }
    }

    private void setSongForAllPlayers(Game currentGame, Card card) {
        for (User player : currentGame.getPlayers()) {
            User fetchedPlayer = userService.findUserByUserId(player.getUserId());
            SpotifyService.setSong(
                    fetchedPlayer.getSpotifyJWT().getAccessToken(),
                    fetchedPlayer.getSpotifyDeviceId(),
                    card.getSongId());
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
        Map<Integer, CardState> cardsState = new HashMap<>();

        for (Card card : cards) {
            cardsState.put(card.getCardId(), card.getCardState());
        }
        return cardsState;
    }

    private Game handleMatch(Game currentGame, Turn currentTurn) {
        if (checkMatch(currentGame, currentTurn)) {
            if (isCompleteSet(currentGame, currentTurn)) {
                winPoints(currentGame, currentTurn);
                addMatchCount(currentGame);
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
        currentGame.getScoreBoard().compute(userId, (k, score) -> score + currentGame.getGameParameters().getNumOfCardsPerSet());
        setCardsExcluded(currentGame, currentTurn);
    }

    private void setCardsFaceDown(Game currentGame, Turn currentTurn){
        CardCollection cardCollection = currentGame.getCardCollection();

        for (int cardId : currentTurn.getPicks()){
            cardCollection.getCardById(cardId).setCardState(CardState.FACEDOWN);
        }
    }

    private void setCardsExcluded(Game currentGame, Turn currentTurn){
        CardCollection cardCollection = currentGame.getCardCollection();

        for (int cardId : currentTurn.getPicks()){
            cardCollection.getCardById(cardId).setCardState(CardState.EXCLUDED);
        }
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
/*                .scoreBoard(
                        new WSScoreBoardChanges()) // TODO: set WSScoreBoardChanges()*/
                .build();

        eventPublisher.publishEvent(new GameChangesEvent(this, currentGame.getGameId(), wsGameChangesDto));
    }

    private boolean checkFinished(Game currentGame){
        return (currentGame.getMatchCount() == currentGame.getGameParameters().getNumOfSets());
    }

    private void finishGame(Game currentGame){
        currentGame.setGameState(GameState.FINISHED);
        recordGameStatistics(currentGame);
        //resetGame(currentGame);
        inMemoryGameRepository.save(currentGame);
    }

    private void recordGameStatistics(Game currentGame){
        Integer gameStatsId = currentGame.getGameStatsId();
        List<User> players = currentGame.getPlayers();
        HashMap<Long, Long> scores = currentGame.getScoreBoard();
        Long winningScore = scores.values().stream()
                        .max(Long::compareTo).orElseThrow();

        for (User player: players){
            Stats stats = new Stats();
            Long userId = player.getUserId();
            stats.setUserId(userId);
            stats.setGameId(gameStatsId);
            stats.setSetsWon(scores.get(player.getUserId()));
            // With max scores wins, else losses
            if (Objects.equals(scores.get(userId), winningScore)){
                stats.setWin(true);
                stats.setLoss(false);
                stats.setAborted(false);
            } else {
                stats.setWin(false);
                stats.setLoss(true);
                stats.setAborted(false);
            }
            statsService.saveStats(stats);
        }
    }

    private void resetGame(Game currentGame){
        currentGame.setGameState(GameState.OPEN);
        currentGame.setHistory(null);
        currentGame.setActivePlayer(null);
        currentGame.setScoreBoard(null);
        currentGame.setMatchCount(null);
        currentGame.setCardCollection(null);
        currentGame.setGameStatsId(null);

        inMemoryGameRepository.save(currentGame);
    }

    private void publishGamefinished(Game currentGame){

        WSGameChangesDto wsGameChangesDto = WSGameChangesDto.builder()
                .gameChangesDto(WSGameChanges.builder()
                        .gameState(currentGame.getGameState()).build())
/*                .scoreBoard(
                        new WSScoreBoardChanges()) // TODO: set WSScoreBoardChanges()*/
                .build();

        eventPublisher.publishEvent(new GameChangesEvent(this, currentGame.getGameId(), wsGameChangesDto));
    }

    /*
     * HELPER METHODS FOR WEBSOCKET UPDATES
     * */

    private void sendPlayersChangedWsDto(Integer gameId, List<User> users) {
        List<PlayerDTO> players = userService.getPlayerDTOListFromListOfUsers(users);

        eventPublisher.publishEvent(new LobbyOverviewChangedEvent(this, gameId, players));


        WSGameChangesDto wsGameChangesDto = WSGameChangesDto.builder()
                .gameChangesDto(WSGameChanges.builder().playerList(players).build()).build();

        eventPublisher.publishEvent(new GameChangesEvent(this, gameId, wsGameChangesDto));
    }

    private void sendGameStateChangedWsDto(Integer gameId, GameState gameState) {
        eventPublisher.publishEvent(new LobbyOverviewChangedEvent(this, gameId, gameState));

        WSGameChangesDto wsGameChangesDto = WSGameChangesDto.builder()
                .gameChangesDto(WSGameChanges.builder().gameState(gameState).build()).build();

        eventPublisher.publishEvent(new GameChangesEvent(this, gameId, wsGameChangesDto));

    }

}
