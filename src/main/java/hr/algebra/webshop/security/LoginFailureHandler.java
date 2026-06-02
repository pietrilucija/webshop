package hr.algebra.webshop.security;

import hr.algebra.webshop.service.LoginHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final LoginHistoryService loginHistoryService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String email = request.getParameter("username");
        loginHistoryService.recordLogin(
                email != null ? email : "unknown",
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                false);

        response.sendRedirect("/auth/login?error");
    }
}