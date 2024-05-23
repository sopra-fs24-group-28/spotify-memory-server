# Spotymemory (Sopra Group 28)
[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-orange.svg)](https://sonarcloud.io/summary/new_code?id=sopra-fs24-group-28_spotify-memory-server)

[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=sopra-fs24-group-28_spotify-memory-server)](https://sonarcloud.io/summary/new_code?id=sopra-fs24-group-28_spotify-memory-server)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=sopra-fs24-group-28_spotify-memory-server&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=sopra-fs24-group-28_spotify-memory-server)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=sopra-fs24-group-28_spotify-memory-server&metric=coverage)](https://sonarcloud.io/summary/new_code?id=sopra-fs24-group-28_spotify-memory-server)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=sopra-fs24-group-28_spotify-memory-server&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=sopra-fs24-group-28_spotify-memory-server)

## Introduction
While music streaming services have transformed the listening experience of everybody over the past decade, they have also deteriorated the social aspects of music enjoyment. In the era of streaming, it is difficult to discover, share and enjoy music with friends.

This project intends to provide a playful shared listening experience in the form of a Spotify based memory game. Memory is a game in which players take turns to collect points by matching pairs of face-down cards. In our game, Spotymemory, the cards represent elements of music, such as snippets of a song or album art, depending on the game mode.

Through this game, players have the chance to discover new music with friends. This game is also a great web application project for this course, as it takes advantage of the powerful Spotify API and presents a broad range of design possibilities. 

**Disclaimer: Limitations imposed by the Spotify API**

The app can only be used by people which fulfill both of the following:
* Have a valid spotify premium subscription.
* Have their account registered for usage in this app (contact the admin).

## Technologies
The back-end is implemented in Java with the Spring Boot framework, utilizing JPA for data persistence. The application is deployed on Google Cloud, and communication between the server and client is handled via REST and WebSockets, with STOMP employed as an additional messaging protocol for WebSockets.

## Components - High-level overview
The [controller classes](https://github.com/sopra-fs24-group-28/spotify-memory-server/tree/main/src/main/java/ch/uzh/ifi/hase/soprafs24/controller) receive all REST calls and pass them onto the Services. The core logic of the game is implemented within [GameService](https://github.com/sopra-fs24-group-28/spotify-memory-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/service/GameService.java), utilizing [SpotifyService](https://github.com/sopra-fs24-group-28/spotify-memory-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/service/SpotifyService.java) for all Spotify related communication.

## Launch & Development

**Setup required:** Save "clientSecret", "redirectURL" (for Spotify), as well as "GCP_SERVICE_CREDENTIALS" (for google cloud) in the system environment variables.

### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew bootRun
```

### Test

```bash
./gradlew test
```

## Roadmap

Following extensions are planned for the future:
* Additional Game Mode: Two different Songs of one Artist
* Obtain Quota Extension (thereafter, the app can used publicly without registration)
* Leaderboard across all players

## Authors & Acknowledgements

Group 28 consists of [Diyar Taskiran](https://github.com/DTaskiran), [Elias Müller](https://github.com/EliasWJMuller), [Hyeongseok Kim](https://github.com/hs-kim1990), [Nicolas Schuler](https://github.com/NicSchuler), and [Niklas Schmidt](https://github.com/niklasschm1dt).

Furthermore, we want to thank our TA Cedric von Rauscher for the valuable inputs throughout the semester, as well as the whole teaching team.

## License

See [LICENSE](https://github.com/sopra-fs24-group-28/spotify-memory-server/blob/main/LICENSE)