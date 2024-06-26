package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.game.CardState;
import ch.uzh.ifi.hase.soprafs24.constant.game.GameCategory;
import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.constant.user.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Stats;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.event.InactiveRequestEvent;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameConstant;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.model.game.Turn;
import ch.uzh.ifi.hase.soprafs24.model.game.Card;
import ch.uzh.ifi.hase.soprafs24.model.game.CardCollection;
import ch.uzh.ifi.hase.soprafs24.repository.inMemory.InMemoryGameRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerDTO;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.WSGameChangesDto;
import ch.uzh.ifi.hase.soprafs24.websocket.dto.helper.*;
import ch.uzh.ifi.hase.soprafs24.websocket.events.GameChangesEvent;
import ch.uzh.ifi.hase.soprafs24.websocket.events.LobbyOverviewChangedEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Service
@Transactional
@AllArgsConstructor
public class GameService {

    private UserService userService;
    private StatsService statsService;
    private InMemoryGameRepository inMemoryGameRepository;
    private ApplicationEventPublisher eventPublisher;
    private DeferredExecutionService deferredExecutionService;

    private final ConcurrentHashMap<Integer, Boolean> isUpdating = new ConcurrentHashMap<>();

    private void setGameInCalcStatus(Integer id, Boolean newVal) {
        isUpdating.put(id, newVal);
    }

    private boolean isGameInCalc(Integer id) {
        return id != null && isUpdating.get(id) != null && isUpdating.get(id);
    }


    public Game createGame(GameParameters gameParameters) {

        User host = UserContextHolder.getCurrentUser();
        if (host.getCurrentGameId() != null || host.getState().equals(UserStatus.INGAME)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already in a game." + host.getCurrentGameId());
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
        if (currentGame.getPlayers().size() >= GameConstant.getMinPlayers() && Objects.equals(currentGame.getHostId(), hostId) && currentGame.getGameState() == GameState.OPEN){
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
                    .activePlayerStreakActive(streakMultiplierIsActive(currentGame))
                    .build();

            WSCardsStates wsCardsStates = new WSCardsStates(currentGame.getCardCollection().getAllCardStates());

            WSGameChangesDto wsGameChangesDto = WSGameChangesDto.builder()
                    .gameChangesDto(wsGameChanges)
                    .cardsStates(wsCardsStates)
                    .build();

            eventPublisher.publishEvent(new GameChangesEvent(this, gameId, wsGameChangesDto));
        } else if (currentGame.getGameState() == GameState.ONPLAY) {
            // this tries to ensure that double-clicking "Start" doesn't start the game multiple times
            System.out.println("Game " + currentGame.getGameId() + " is already ONPLAY!");
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
            currentGame.setActivePlayerStreak(0);
        } else {
            int streak = currentGame.getActivePlayerStreak();
            currentGame.setActivePlayerStreak(streak+1);
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
         sendPlayersChangedWsDto(game, users);
     }

     public void removePlayerFromGame(Integer gameId) {
         User userToRemove = UserContextHolder.getCurrentUser();
         removePlayerHelper(gameId, userToRemove);
         pausePlaybackHelper(userToRemove);
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
                 game.getScoreBoard().remove(userToRemove.getUserId());
             }
             sendPlayersChangedWsDto(game, users);
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
            setGameInCalcStatus(currentGame.getGameId(), true);
            try {
                if (checkActivePlayer(currentGame) && checkActiveCard(currentGame, cardId)) {
                    pausePlaybackAllPlayers(currentGame);
                    runActiveTurn(currentGame, cardId);
                    if (inMemoryGameRepository.findById(currentGame.getGameId()) != null) {
                        inMemoryGameRepository.save(currentGame);
                    }
                }
            } finally {
                setGameInCalcStatus(currentGame.getGameId(), false);
                eventPublisher.publishEvent(new InactiveRequestEvent(this, gameId));
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

        handleMatch(currentGame, currentTurn);

        if (checkFinished(currentGame)){
            finishGame(currentGame);
            publishGamefinished(currentGame);
            pausePlaybackAllPlayers(currentGame);

            resetGame(currentGame);
            if (inMemoryGameRepository.findById(currentGame.getGameId()) != null) {
                sendGameStateChangedWsDto(currentGame.getGameId(), currentGame.getGameState());
            }
        } else {
        publishOnPlayState(currentGame);
        }
    }

    public void handleInactivePlayer(Integer gameId) {
        if (isGameInCalc(gameId)) {
            deferredExecutionService.deferTask(gameId, () -> executeInactivePlayerLogic(gameId));
        } else {
            updateOnInactivity(gameId);
        }
    }

    @EventListener
    public void onGameCalculationStatusChange(InactiveRequestEvent event) {
        if (deferredExecutionService.hasDeferredTask(event.getGameId())) {
            deferredExecutionService.executeDeferredTask(event.getGameId());
        }
    }

    private void executeInactivePlayerLogic(Integer gameId) {
        Game currentGame = inMemoryGameRepository.findById(gameId);

        int historySize = currentGame.getHistory().size();

        // We need to ensure that a correct selection at the very end of a turn does not in a wrongful change of Turn
        if (!(historySize > 1 &&
                (currentGame.getHistory().get(historySize - 1).getPicks().isEmpty()
                        && currentGame.getHistory().get(historySize - 2).getUserId() == currentGame.getActivePlayer()))) {updateOnInactivity(gameId);}
    }

    private void updateOnInactivity(Integer gameId){
        User inactivePlayer = UserContextHolder.getCurrentUser();
        Game currentGame = inMemoryGameRepository.findById(gameId);

        if (inactivePlayer.getUserId().equals(currentGame.getActivePlayer())) {
            setCardsFaceDown(currentGame, currentGame.getHistory().get(currentGame.getHistory().size()-1));
            initiateNewTurn(currentGame, false);
            pausePlaybackAllPlayers(currentGame);

            inMemoryGameRepository.save(currentGame);

            WSGameChangesDto wsGameChangesDto = WSGameChangesDto.builder()
                    .cardsStates(new WSCardsStates(currentGame.getCardCollection().getAllCardStates()))
                    .gameChangesDto(WSGameChanges.builder().activePlayer(currentGame.getActivePlayer()).build())
                    .build();

            eventPublisher.publishEvent(new GameChangesEvent( this, gameId, wsGameChangesDto));
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

    private void pausePlaybackAllPlayers(Game currentGame) {
        if (currentGame.getGameParameters().getGameCategory() == GameCategory.STANDARDSONG) {
            for (User player : currentGame.getPlayers()) {
                User fetchedPlayer = userService.findUserByUserId(player.getUserId());
                pausePlaybackHelper(fetchedPlayer);
            }
        }
    }

    private void pausePlaybackHelper(User userToPause) {
        try {
            SpotifyService.pausePlayback(
                    userToPause.getSpotifyJWT().getAccessToken(),
                    userToPause.getSpotifyDeviceId());
        } catch (ResponseStatusException e) {
            System.out.println("Couldn't pause playback for user " + userToPause.getUserId());
        }
    }

    private void publishCardContents(Game currentGame, Card card){

        WSGameChangesDto wsGameChangesDto = WSGameChangesDto.builder()
                .cardContent(cardContentsHelper(currentGame))
                .cardsStates(new WSCardsStates(mapCardsState(currentGame.getCardCollection())))
                .build();

        eventPublisher.publishEvent(new GameChangesEvent(this, currentGame.getGameId(), wsGameChangesDto));
    }

    private Map<Integer, CardState> mapCardsState(CardCollection cardCollection) {
        List<Card> cards = cardCollection.getCards();
        Map<Integer, CardState> cardsState = new HashMap<>();

        for (Card card : cards) {
            cardsState.put(card.getCardId(), card.getCardState());
        }
        return cardsState;
    }


    private WSCardContents cardContentsHelper(Game game) {

        WSCardContents wsCardContents = WSCardContents.builder()
                .cardContents(new ArrayList<>())
                .build();

        ArrayList<ArrayList<String>> cardContents = new ArrayList<>();
        List<Integer> currentCards = game.getHistory().get(game.getHistory().size()-1).getPicks();

        for (Integer cardId : currentCards) {
            Card card = game.getCardCollection().getCardById(cardId);
            wsCardContents.addCardContent(card.getCardId(), card.getSongId(), card.getImageUrl());
        }
        return wsCardContents;
    }

    private void handleMatch(Game currentGame, Turn currentTurn) throws InterruptedException {
        if (isCompleteSet(currentGame, currentTurn)) {
            if (checkMatch(currentGame, currentTurn)) {
                winPoints(currentGame, currentTurn);
                addMatchCount(currentGame);
                initiateNewTurn(currentGame, true);
            } else {
                setCardsFaceDown(currentGame, currentTurn);
                initiateNewTurn(currentGame, false);
            }
            Thread.sleep(GameConstant.getViewSleep());
            pausePlaybackAllPlayers(currentGame);
        }
    }


    private boolean checkMatch(Game currentGame, Turn currentTurn){
        return currentGame.getCardCollection().checkMatch(currentTurn.getPicks());
    }

    private boolean isCompleteSet(Game currentGame, Turn currentTurn){
        return currentTurn.getPicks().size() == currentGame.getGameParameters().getNumOfCardsPerSet();
    }

    private void winPoints(Game currentGame, Turn currentTurn) {
        Long userId = UserContextHolder.getCurrentUser().getUserId();
        int multiplier = streakMultiplierIsActive(currentGame) ? currentGame.getGameParameters().getStreakMultiplier() : 1;
        final int newPoints = currentGame.getGameParameters().getNumOfCardsPerSet() * multiplier;
        currentGame.getScoreBoard().compute(userId, (k, score) -> score + newPoints);
        setCardsExcluded(currentGame, currentTurn);
    }

    private Boolean streakMultiplierIsActive(Game currentGame) {
        int activeStreak = currentGame.getActivePlayerStreak();
        int streakStart = currentGame.getGameParameters().getStreakStart();
        return activeStreak >= streakStart;
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
    }

    private void publishOnPlayState(Game currentGame){
        WSGameChangesDto wsGameChangesDto;


        if (currentGame.getHistory().get(currentGame.getHistory().size()-1).getPicks().isEmpty()) {
            wsGameChangesDto = WSGameChangesDto.builder()
                    .gameChangesDto(WSGameChanges.builder()
                            .activePlayer(currentGame.getActivePlayer())
                            .activePlayerStreakActive(streakMultiplierIsActive(currentGame))
                            .build())
                    .cardsStates(
                            new WSCardsStates(mapCardsState(currentGame.getCardCollection())))
                    .scoreBoard(
                            new WSScoreBoardChanges(currentGame.getScoreBoard()))
                    .build();
        } else {
            wsGameChangesDto = WSGameChangesDto.builder()
                    .cardsStates(
                            new WSCardsStates(mapCardsState(currentGame.getCardCollection())))
                    .cardContent(cardContentsHelper(currentGame))
                    .scoreBoard(
                            new WSScoreBoardChanges(currentGame.getScoreBoard()))
                    .build();
        }

        eventPublisher.publishEvent(new GameChangesEvent(this, currentGame.getGameId(), wsGameChangesDto));
    }

    private boolean checkFinished(Game currentGame){
        return (currentGame.getMatchCount() == currentGame.getGameParameters().getNumOfSets());
    }

    private void finishGame(Game currentGame){
        currentGame.setGameState(GameState.OPEN);
        recordGameStatistics(currentGame);
        //resetGame(currentGame);
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
        //set game to initial value
        currentGame.setGameState(GameState.OPEN);
        currentGame.setHistory(new ArrayList<>());
        currentGame.setActivePlayer(null);
        currentGame.setScoreBoard(new HashMap<>());
        currentGame.setMatchCount(null);
        currentGame.setCardCollection(null);
        currentGame.setGameStatsId(null);
        currentGame.setQuickTurn(new HashMap<>());
        currentGame.setQuickTurnActive(false);
    }

    private void publishGamefinished(Game currentGame){

        WSGameChangesDto wsGameChangesDto = WSGameChangesDto.builder()
                .gameChangesDto(WSGameChanges.builder()
                        .gameState(currentGame.getGameState()).build())
                .scoreBoard(
                        new WSScoreBoardChanges(currentGame.getScoreBoard()))
                .build();

        eventPublisher.publishEvent(new GameChangesEvent(this, currentGame.getGameId(), wsGameChangesDto));
    }

    /*
     * HELPER METHODS FOR WEBSOCKET UPDATES
     * */

    private void sendPlayersChangedWsDto(Game game, List<User> users) {
        List<PlayerDTO> players = userService.getPlayerDTOListFromListOfUsers(users);
        Integer gameId = game.getGameId();

        eventPublisher.publishEvent(new LobbyOverviewChangedEvent(this, gameId, players));


        WSGameChangesDto.WSGameChangesDtoBuilder wsGameChangesDtoBuilder = WSGameChangesDto.builder()
                .gameChangesDto(WSGameChanges.builder().playerList(players).build());

        if (game.getGameState() == GameState.ONPLAY) {
            WSScoreBoardChanges wsScoreBoardChanges = new WSScoreBoardChanges(game.getScoreBoard());
            wsGameChangesDtoBuilder.scoreBoard(wsScoreBoardChanges);
        }

        eventPublisher.publishEvent(new GameChangesEvent(this, gameId, wsGameChangesDtoBuilder.build()));
    }

    private void sendGameStateChangedWsDto(Integer gameId, GameState gameState) {
        eventPublisher.publishEvent(new LobbyOverviewChangedEvent(this, gameId, gameState));

        WSGameChangesDto wsGameChangesDto = WSGameChangesDto.builder()
                .gameChangesDto(WSGameChanges.builder().gameState(gameState).build()).build();

        eventPublisher.publishEvent(new GameChangesEvent(this, gameId, wsGameChangesDto));

    }

}
