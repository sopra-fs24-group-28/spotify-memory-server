package ch.uzh.ifi.hase.soprafs24.rest.webFilter;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

class ManualSecurityFilterTest {

    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private FilterChain mockFilterChain;
    @Mock
    private AuthService authService;

    @InjectMocks
    private ManualSecurityFilter manualSecurityFilter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenPostAuthRequest_thenProceedWithoutAuthorization() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/auth");
        when(mockRequest.getMethod()).thenReturn("POST");

        manualSecurityFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
        verify(mockResponse, never()).sendError(anyInt(), anyString());
    }

    @Test
    void whenAccessWsEndpoint_thenProceedWithoutAuthorization() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/ws");

        manualSecurityFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
        verify(mockResponse, never()).sendError(anyInt(), anyString());
    }

    @Test
    void whenUnauthorized_thenRespondWithUnauthorizedError() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/other");
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer invalidtoken");

        when(authService.getUserBySessionToken("invalidtoken")).thenReturn(null);

        manualSecurityFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, never()).doFilter(mockRequest, mockResponse);
        verify(mockResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid Token");
    }

    @Test
    void whenAuthorized_thenProceedWithFilterChain() throws Exception {
        User user = new User();
        user.setUserId(1L);

        when(mockRequest.getRequestURI()).thenReturn("/other");
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer validtoken");
        when(authService.getUserBySessionToken("validtoken")).thenReturn(user);

        manualSecurityFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
        verify(mockResponse, never()).sendError(anyInt(), anyString());
    }

    @Test
    void whenNoAuthHeader_thenRespondWithBadRequestError() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/other");

        manualSecurityFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, never()).doFilter(mockRequest, mockResponse);
        verify(mockResponse).sendError(HttpServletResponse.SC_BAD_REQUEST, "Unauthorized: Missing Authorization Header");
    }
}
