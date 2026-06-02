package hr.algebra.webshop.listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class SessionTrackingListener implements HttpSessionListener {

    private final AtomicInteger activeSessions = new AtomicInteger(0);

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        int count = activeSessions.incrementAndGet();
        log.info("Sesija kreirana: {}. Aktivnih sesija: {}",
                event.getSession().getId(), count);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        int count = activeSessions.decrementAndGet();
        log.info("Sesija uništena: {}. Aktivnih sesija: {}",
                event.getSession().getId(), count);
    }

    public int getActiveSessions() {
        return activeSessions.get();
    }
}