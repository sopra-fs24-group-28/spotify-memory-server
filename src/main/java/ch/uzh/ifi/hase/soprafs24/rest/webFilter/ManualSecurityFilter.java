package ch.uzh.ifi.hase.soprafs24.rest.webFilter;

import ch.uzh.ifi.hase.soprafs24.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@WebFilter
@Order(1)
@AllArgsConstructor
public class ManualSecurityFilter  implements Filter {
    private final AuthService authService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestUri = request.getRequestURI();

        if (
                (requestUri.startsWith("/auth")
                && ("POST".equalsIgnoreCase(request.getMethod()) || "GET".equalsIgnoreCase(request.getMethod()) ))
                || authService.validateUserBySessionTokenAndSetContext(request.getHeader("Authorization"))) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Bad Credentials");
        }
    }

}
