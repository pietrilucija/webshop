package hr.algebra.webshop.controller.api;

import hr.algebra.webshop.dto.request.LoginRequest;
import hr.algebra.webshop.dto.request.RegisterRequest;
import hr.algebra.webshop.dto.response.JwtResponse;
import hr.algebra.webshop.model.RefreshToken;
import hr.algebra.webshop.model.User;
import hr.algebra.webshop.repository.RefreshTokenRepository;
import hr.algebra.webshop.security.JwtTokenProvider;
import hr.algebra.webshop.service.LoginHistoryService;
import hr.algebra.webshop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private static final String ERROR_KEY = "error";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginHistoryService loginHistoryService;

    @PostMapping("/login")
    public ResponseEntity<Object> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getEmail(), request.getPassword()));

            User user = userService.findByEmail(request.getEmail());
            String accessToken = jwtTokenProvider
                    .generateAccessToken(user.getEmail(),
                            user.getRole().name());
            String refreshToken = jwtTokenProvider
                    .generateRefreshToken(user.getEmail());

            saveRefreshToken(user, refreshToken);

            loginHistoryService.recordLogin(
                    user.getEmail(),
                    httpRequest.getRemoteAddr(),
                    httpRequest.getHeader("User-Agent"),
                    true);

            return ResponseEntity.ok(JwtResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build());

        } catch (BadCredentialsException e) {
            loginHistoryService.recordLogin(
                    request.getEmail(),
                    httpRequest.getRemoteAddr(),
                    httpRequest.getHeader("User-Agent"),
                    false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(ERROR_KEY, "Neispravni podaci za prijavu"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(
            @Valid @RequestBody RegisterRequest request) {
        try {
            userService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Registracija uspješna"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(ERROR_KEY, e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(@RequestBody Map<String, String> request) {
        String token = request.get("refreshToken");

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(ERROR_KEY, "Nevažeći refresh token"));
        }

        var stored = refreshTokenRepository.findByToken(token);
        if (stored.isEmpty() || stored.get().getExpiresAt()
                .isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(ERROR_KEY, "Refresh token istekao"));
        }

        String email = jwtTokenProvider.getEmailFromToken(token);
        User user = userService.findByEmail(email);

        String newAccessToken = jwtTokenProvider
                .generateAccessToken(email, user.getRole().name());

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    private void saveRefreshToken(User user, String token) {
        refreshTokenRepository.findByUserId(user.getId())
                .ifPresent(refreshTokenRepository::delete);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshToken);
    }
}