package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.PlaylistCollectionDTO;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import ch.uzh.ifi.hase.soprafs24.service.SpotifyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("spotify")
public class SpotifyController {

    @GetMapping("user/playlist/names")
    public ResponseEntity<PlaylistCollectionDTO> getSpotifyUserId(@RequestParam String accessToken) {
        System.out.println(accessToken);
        String aT = UserContextHolder.getCurrentUser().getSpotifyJWT().getAccessToken(); //TODO: remove access token from FE request
        // Call the service method and return its response
        PlaylistCollectionDTO dto = SpotifyService.getUserPlaylistNames(aT);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}




