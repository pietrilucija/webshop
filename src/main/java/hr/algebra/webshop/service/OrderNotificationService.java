package hr.algebra.webshop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class OrderNotificationService {

    @Async
    public void sendOrderConfirmation(String email, Long orderId, BigDecimal total) {
        log.info("[ASYNC] Order confirmation for {} — order #{}, total: {} €",
                email, orderId, total);
        // Replace with JavaMailSender when mail server is configured
    }

    @Async
    public void sendPaymentConfirmation(String email, Long orderId) {
        log.info("[ASYNC] PayPal payment confirmed for {} — order #{}",
                email, orderId);
        // Replace with JavaMailSender when mail server is configured
    }
}