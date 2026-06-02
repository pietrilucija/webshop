package hr.algebra.webshop.service;

import hr.algebra.webshop.dto.response.PayPalOrderResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class PayPalService {

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.return-url}")
    private String returnUrl;

    @Value("${paypal.cancel-url}")
    private String cancelUrl;

    private static final String BASE_URL = "https://api-m.sandbox.paypal.com";
    private final RestTemplate restTemplate = new RestTemplate();

    private String getAccessToken() {
        String credentials = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + credentials);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                BASE_URL + "/v1/oauth2/token",
                HttpMethod.POST, entity,
                new ParameterizedTypeReference<>() {});

        return (String) response.getBody().get("access_token");
    }

    public PayPalOrderResult createPayPalOrder(BigDecimal amount) {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> orderBody = Map.of(
                "intent", "CAPTURE",
                "purchase_units", List.of(Map.of(
                        "amount", Map.of(
                                "currency_code", "EUR",
                                "value", amount.toString()
                        )
                )),
                "application_context", Map.of(
                        "return_url", returnUrl,
                        "cancel_url", cancelUrl
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(orderBody, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                BASE_URL + "/v2/checkout/orders",
                HttpMethod.POST, entity,
                new ParameterizedTypeReference<>() {});

        Map<String, Object> responseBody = response.getBody();
        String orderId = (String) responseBody.get("id");
        String approvalUrl = extractApprovalUrl(responseBody);

        return new PayPalOrderResult(orderId, approvalUrl);
    }

    public boolean captureOrder(String orderId) {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<String> entity = new HttpEntity<>("{}", headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                BASE_URL + "/v2/checkout/orders/" + orderId + "/capture",
                HttpMethod.POST, entity,
                new ParameterizedTypeReference<>() {});

        return "COMPLETED".equals(response.getBody().get("status"));
    }

    @SuppressWarnings("unchecked")
    private String extractApprovalUrl(Map<String, Object> responseBody) {
        List<Map<String, String>> links =
                (List<Map<String, String>>) responseBody.get("links");
        return links.stream()
                .filter(link -> "approve".equals(link.get("rel")))
                .map(link -> link.get("href"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "PayPal approval URL not found in response"));
    }
}