# Contributions of the Team Members Group 28

## Diyar
### Week 1
* [client-#35](https://github.com/sopra-fs24-group-28/spotify-memory-client/issues/35) Spotify Authentication
* [client-#34](https://github.com/sopra-fs24-group-28/spotify-memory-client/issues/34) Routing and Basic Error Screens
* [client-#33](https://github.com/sopra-fs24-group-28/spotify-memory-client/issues/33) =
* [client-#32](https://github.com/sopra-fs24-group-28/spotify-memory-client/issues/32) =

### Week 3
* [#48](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/48) websocket for lobby overview somewhat works

### Week 4
* [#48](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/48) lobby overview websocket data works and is deployable
* [client-#28](https://github.com/sopra-fs24-group-28/spotify-memory-client/issues/28) lobby overview rest request consumed and displayed
* [client-#29](https://github.com/sopra-fs24-group-28/spotify-memory-client/issues/29) lobby overview ws request consumed and overview updated, minor bug found upon deployment

### Week 5
* [client-#20](https://github.com/sopra-fs24-group-28/spotify-memory-client/issues/20) conditional game start affordance for host
* [client-#9](https://github.com/sopra-fs24-group-28/spotify-memory-client/issues/9) correct routing in relation to lobbywaitingroom
* [client-#17](https://github.com/sopra-fs24-group-28/spotify-memory-client/issues/17) send game update over websocket to server
* [client-#19](https://github.com/sopra-fs24-group-28/spotify-memory-client/issues/19) correct routing in relation to game start

### Week 6
* [client-#4](https://github.com/sopra-fs24-group-28/spotify-memory-client/issues/4) Reroute the players to the game screen upon receiving start request
* [client-#7](https://github.com/sopra-fs24-group-28/spotify-memory-client/issues/7) Provide an affordance for the host to restart the game in the game-over screen
* [client-#62](https://github.com/sopra-fs24-group-28/spotify-memory-client/issues/62) Prevent observing players from making direct (card related) actions in the front-end


## Elias
### Week 1
* [#89](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/89) Changes As Requested (Game related Objects and Game creation)
* [#48](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/48) changes ws endpoints (rough setup and first trials)

### Week 3
* [#48](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/48) websocket for lobby overview somewhat works

### Week 4
* [#74](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/74#issue-2203802887) If the host has left the lobby, close the game and WebSocket
* [#73](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/73#issue-2203802872) If the user leaves the game, remove user from Game
* [#41](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/41#issue-2203432033) Open a WebSocket connection from the back-end
* [#48](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/48) lobby overview websocket deployed (with Diyar & Niklas)

### Week 5
* [...] Controller Unit Tests
* [...] WS Testing
* [...] WS Game BE&FE (Diyar & Niklas)

### Week 6
* [...] Solved async issues in game updates (race condition through WS)
* [...] Timing functionality per turn

### Week 7
* [...] Complete Turn Timer Backend and Frontend (Diyar)

## Henry

### Week 1
* [#88](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/88) Create and persist the user object

### Week 3
* [#55] Prepare game state (e.g. Scoreboard, Turn object) (IN progress)
* [#56] Upon valid request to start game, close lobby to new players and start Game (IN Progress)

### Week 4
* [#88](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/88) Persistance with Google cloud SQL
* [#55](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/55) Prepare game state (e.g. Scoreboard, Turn object) (In Progress with helper functions)
* [#56] Upon valid request to start game, close lobby to new players and start Game (pull request)

### Week 5
* [...] Apply game logics within GameService
* [...] Unit tests for GameService

### Week 6
* [#71] update the players game statisticsand persit it in the database
* [...] Unit tests for GameService

### Week 7
* [...] Unit tests for GameService Finished
* [...](https://github.com/sopra-fs24-group-28/spotify-memory-server/pull/165) Reset Game back to open after finished
  

## Nicolas
### Week 1
* [#91](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/91) Get initial Access Token from Spotify
* [#34](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/34) Check validity of User Keys

### Week 2 (Easter Break)
* [#87](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/87) Check if user exists in database, create new entry if does not exist
* [#95](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/95) Create GET auth/token endpoint to send spotify access token to front-end

### Week 3
* [#99](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/99) Get card data from Spotify
* [#100](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/100) Create CardCollection

### Week 4
* [#63](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/63) Helper functions SpotifyService.setSong() and CardCollection.checkMatch()
* Wrote tests for CardCollection functions
* Helped to implement Persistance & get user Playlists

### Week 5
* Unit tests for UserService
* Unit tests for AuthService
* Unit tests for AuthController
* Integration test for running full game
* [#113](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/113) Several minor improvements (Card-Content, Playlist name and Playlist url)
* [#138](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/138) Create POST /spotify/user/deviceid

### Week 6
* [#148](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/148) Collect and save profile pictures
* Improve SpotifyService Error Handling & Update Username/Pictures at login

### Week 7
* [#84](https://github.com/sopra-fs24-group-28/spotify-memory-server/issues/84) Streak Powerup implementation
* Cleanup work

## Niklas
### Week 1
* [#30] Log out functionality
* [#37] create front-end nav-bar component

### Week 2
* [#25] redirect the host to the created lobby after its creation (still in progress)
* [#27] create affordance for the instantiation of a new lobby in the front end

### Week 3
* combine frontend with backend such that endpoints work coherently
* merge dev branch into main (client)

### Week 4
* no issue: made whole allplication responsive
* collaborated with elias on getting websockets running for the overview
* [#18] implemented the Gamescreen including the Gamecards
* implemented logic in inbetween state of games
* [#13 / #15] implemented scoreboard
* no issue: resolved old legacy bugs in frontend

### Week 5
* [...] Playback SDK implementation
* [...] Playback Integration in Game
* [...] Game Screen Impl

### Week 6
* [...] Redisign Game Interface and Misc
* [...] Reciverfunction corrected and optimized
* [...] UX elements (toaster, responsiveness)
