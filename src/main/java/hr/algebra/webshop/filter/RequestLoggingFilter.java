package hr.algebra.webshop.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
@Slf4j
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        long startTime = System.currentTimeMillis();

        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String ip = httpRequest.getRemoteAddr();

        chain.doFilter(request, response);

        long duration = System.currentTimeMillis() - startTime;
        log.info("[{}] {} {} - {}ms (IP: {})",
                method, uri, httpRequest.getQueryString(),
                duration, ip);
    }
}