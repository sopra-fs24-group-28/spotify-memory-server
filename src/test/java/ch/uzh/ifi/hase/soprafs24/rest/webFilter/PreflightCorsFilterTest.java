package ch.uzh.ifi.hase.soprafs24.rest.webFilter;

import org.junit.jupiter.api.Test;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class PreflightCorsFilterTest {

    @Test
    public void whenOptionsRequest_thenSetCorsHeadersAndStatusOK() throws IOException, ServletException {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        FilterChain mockFilterChain = mock(FilterChain.class);

        when(mockRequest.getMethod()).thenReturn("OPTIONS");

        PreflightCorsFilter filter = new PreflightCorsFilter();

        filter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockResponse).setHeader("Access-Control-Allow-Origin", "*");
        verify(mockResponse).setHeader("Access-Control-Allow-Credentials", "true");
        verify(mockResponse).setHeader("Access-Control-Allow-Methods", "PUT, GET, POST, DELETE, PATCH");
        verify(mockResponse).setHeader("Access-Control-Allow-Headers", "*");
        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);

        verify(mockFilterChain, times(0)).doFilter(mockRequest, mockResponse);
    }
}
