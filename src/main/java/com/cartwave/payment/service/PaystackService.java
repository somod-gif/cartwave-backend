package com.cartwave.payment.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.cartwave.exception.BusinessException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

/**
 * Paystack payment gateway integration.
 *
 * <p>Handles transaction initialization and HMAC-SHA512 webhook signature
 * verification as documented at https://paystack.com/docs/api/
 */
@Service
@Slf4j
public class PaystackService {

    private static final String HMAC_ALGORITHM = "HmacSHA512";

    @Value("${cartwave.paystack.secret-key:}")
    private String secretKey;

    @Value("${cartwave.paystack.base-url:https://api.paystack.co}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public PaystackService() {
        this.restTemplate = new RestTemplate();
    }

    // ── Transaction Initialization ────────────────────────────────────────────

    /**
     * Calls POST /transaction/initialize on Paystack and returns the
     * authorization URL to redirect the customer to.
     *
     * @param email     customer email
     * @param amount    amount in the store's currency (converted to kobo/pesewa internally)
     * @param reference unique transaction reference
     * @param currency  ISO 4217 currency code (e.g. "NGN", "GHS", "ZAR", "USD")
     * @return PaystackInitResponse containing authorizationUrl and accessCode
     */
    public PaystackInitResponse initializeTransaction(String email,
                                                       BigDecimal amount,
                                                       String reference,
                                                       String currency) {
        requireSecretKey();

        // Paystack expects amount in smallest currency unit (kobo for NGN)
        long amountInSmallestUnit = amount.multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, Object> payload = Map.of(
                "email", email,
                "amount", amountInSmallestUnit,
                "reference", reference,
                "currency", currency
        );

        HttpHeaders headers = buildAuthHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            PaystackApiResponse response = restTemplate.postForObject(
                    baseUrl + "/transaction/initialize",
                    entity,
                    PaystackApiResponse.class
            );

            if (response == null || !Boolean.TRUE.equals(response.getStatus())) {
                throw new BusinessException("PAYSTACK_INIT_FAILED",
                        "Paystack initialization failed: " + (response != null ? response.getMessage() : "no response"));
            }

            return response.getData();
        } catch (RestClientException ex) {
            log.error("Paystack transaction initialization error for ref={}: {}", reference, ex.getMessage());
            throw new BusinessException("PAYSTACK_GATEWAY_ERROR", "Unable to reach Paystack gateway: " + ex.getMessage());
        }
    }

    // ── Webhook Signature Verification ───────────────────────────────────────

    /**
     * Verifies the HMAC-SHA512 signature sent by Paystack in the
     * {@code X-Paystack-Signature} header.
     *
     * @param rawBody   the raw request body bytes as received from Paystack
     * @param signature the value of the X-Paystack-Signature header
     * @return true if the signature is valid; false otherwise
     */
    public boolean verifyWebhookSignature(byte[] rawBody, String signature) {
        if (secretKey == null || secretKey.isBlank()) {
            log.warn("Paystack secret key not configured — skipping webhook signature verification");
            return false;
        }
        if (signature == null || signature.isBlank()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] computed = mac.doFinal(rawBody);
            String computedHex = HexFormat.of().formatHex(computed);
            return constantTimeEquals(computedHex, signature.toLowerCase());
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            log.error("Paystack signature verification error: {}", ex.getMessage());
            return false;
        }
    }

    // ── Transaction Verification ──────────────────────────────────────────────

    /**
     * Calls GET /transaction/verify/:reference to confirm payment status
     * from Paystack's side (useful for manual confirmation flows).
     *
     * @param reference the transaction reference
     * @return PaystackTransactionData containing status and payment details
     */
    public PaystackTransactionData verifyTransaction(String reference) {
        requireSecretKey();
        HttpHeaders headers = buildAuthHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            PaystackVerifyResponse response = restTemplate.exchange(
                    baseUrl + "/transaction/verify/" + reference,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    PaystackVerifyResponse.class
            ).getBody();

            if (response == null || !Boolean.TRUE.equals(response.getStatus())) {
                throw new BusinessException("PAYSTACK_VERIFY_FAILED",
                        "Transaction verification failed: " + (response != null ? response.getMessage() : "no response"));
            }

            return response.getData();
        } catch (RestClientException ex) {
            log.error("Paystack transaction verify error for ref={}: {}", reference, ex.getMessage());
            throw new BusinessException("PAYSTACK_GATEWAY_ERROR", "Paystack verify call failed: " + ex.getMessage());
        }
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private HttpHeaders buildAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(secretKey);
        return headers;
    }

    private void requireSecretKey() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new BusinessException("PAYSTACK_NOT_CONFIGURED",
                    "Paystack secret key is not configured. Set PAYSTACK_SECRET_KEY env variable.");
        }
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    // ── Response DTOs (inner classes) ─────────────────────────────────────────

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaystackApiResponse {
        private Boolean status;
        private String message;
        private PaystackInitResponse data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaystackInitResponse {
        @JsonProperty("authorization_url")
        private String authorizationUrl;
        @JsonProperty("access_code")
        private String accessCode;
        private String reference;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaystackVerifyResponse {
        private Boolean status;
        private String message;
        private PaystackTransactionData data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaystackTransactionData {
        /** e.g. "success", "failed", "abandoned" */
        private String status;
        private String reference;
        /** Amount in kobo/pesewa */
        private Long amount;
        private String currency;
        @JsonProperty("paid_at")
        private String paidAt;
        @JsonProperty("gateway_response")
        private String gatewayResponse;
    }
}
