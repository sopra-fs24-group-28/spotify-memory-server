package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Stats;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.StatsRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserStatsDTO;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.PersistenceException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StatsServiceTest {
    @Mock
    private StatsRepository statsRepository;

    @InjectMocks
    private StatsService statsService;

    private Stats testStats;

    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        testStats = new Stats();
        testUser = new User();
        testUser.setUserId(1L);

        when(statsRepository.saveAndFlush(Mockito.any())).thenReturn(testStats);
    }

    @Test
    public void getLatestGameId_success() {
        // mock repository functions
        when(statsRepository.findMaxGameID()).thenReturn(1);

        Integer returnValue = statsService.getLatestGameId();
        // assert user status
        assertEquals(1, returnValue);
    }

    @Test
    public void saveStats_success() {
        Stats returnValue = statsService.saveStats(testStats);
        // assert user status
        assertEquals(testStats, returnValue);
    }

    @Test
    public void getCurrentUserStats_validReturn_success() {
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
            when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
            when(statsRepository.countByUserId(Mockito.any())).thenReturn(6);
            when(statsRepository.countByUserIdAndWinIsTrue(Mockito.any())).thenReturn(3);
            when(statsRepository.countByUserIdAndLossIsTrue(Mockito.any())).thenReturn(2);
            when(statsRepository.countByUserIdAndAbortedIsTrue(Mockito.any())).thenReturn(1);
            when(statsRepository.sumSetsWonByUserId(Mockito.any())).thenReturn(10L);

            UserStatsDTO userStatsDTO = statsService.getCurrentUserStats();

            // assert user status
            assertEquals(1, userStatsDTO.getUserId());
            assertEquals(6, userStatsDTO.getTotalGames());
            assertEquals(3, userStatsDTO.getGamesWon());
            assertEquals(2, userStatsDTO.getGamesLoss());
            assertEquals(1, userStatsDTO.getGamesAborted());
            assertEquals(10L, userStatsDTO.getTotalSetsWon());
        }
    }

    @Test
    public void getCurrentUserStats_invalidReturn_exceptionThrown() {
        try (MockedStatic<UserContextHolder> mockedUserContext = mockStatic(UserContextHolder.class)) {
            when(UserContextHolder.getCurrentUser()).thenReturn(testUser);
            when(statsRepository.countByUserId(Mockito.any())).thenReturn(5);
            when(statsRepository.countByUserIdAndWinIsTrue(Mockito.any())).thenReturn(3);
            when(statsRepository.countByUserIdAndLossIsTrue(Mockito.any())).thenReturn(2);
            when(statsRepository.countByUserIdAndAbortedIsTrue(Mockito.any())).thenReturn(1);
            when(statsRepository.sumSetsWonByUserId(Mockito.any())).thenReturn(10L);

            PersistenceException thrown = assertThrows(PersistenceException.class, () -> statsService.getCurrentUserStats());
            assertEquals(thrown.getMessage(),"Error in the number of games");
        }
    }
}
