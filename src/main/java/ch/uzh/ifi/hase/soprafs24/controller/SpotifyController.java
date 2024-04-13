package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.PlaylistCollectionDTO;
import ch.uzh.ifi.hase.soprafs24.service.SpotifyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/playlist")
public class SpotifyController {

    @GetMapping("/names")
    public ResponseEntity<PlaylistCollectionDTO> getSpotifyUserId(@RequestParam String accessToken) {
        System.out.println(accessToken);
        // Call the service method and return its response
        PlaylistCollectionDTO dto = SpotifyService.getUserPlaylistNames(accessToken);

        if (dto != null) {
            // If playlistDTO is not null, return it with HttpStatus.OK
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } else {
            // If there was an error or no playlists were found, return HttpStatus.INTERNAL_SERVER_ERROR
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}




