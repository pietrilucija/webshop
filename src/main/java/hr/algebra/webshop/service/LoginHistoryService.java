package hr.algebra.webshop.service;

import hr.algebra.webshop.model.LoginHistory;
import hr.algebra.webshop.model.User;
import hr.algebra.webshop.repository.LoginHistoryRepository;
import hr.algebra.webshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;

    @Async
    public void recordLogin(String email, String ipAddress,
                            String userAgent, boolean success) {
        userRepository.findByEmail(email).ifPresent(user -> {
            LoginHistory history = LoginHistory.builder()
                    .user(user)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .success(success)
                    .build();
            loginHistoryRepository.save(history);
        });
    }

    public Page<LoginHistory> findAll(Pageable pageable) {
        return loginHistoryRepository.findAllByOrderByLoginAtDesc(pageable);
    }
}