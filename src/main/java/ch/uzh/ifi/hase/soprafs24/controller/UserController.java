package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserStatsDTO;
import ch.uzh.ifi.hase.soprafs24.service.StatsService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.PersistenceException;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final StatsService statsService;

    @GetMapping("/profiles")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO getUserProfile() {
        try{
            PlayerDTO currentUserDTD = userService.getPlayerDTOForCurrentUser();
            UserStatsDTO userStatsDTO = statsService.getCurrentUserStats();
            return new UserGetDTO(currentUserDTD, userStatsDTO);
        } catch (PersistenceException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("User profile unavailable."));
        }
    }




}
