package app.lifelinq.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JwtVerifier {
    private static final Pattern STRING_CLAIM = Pattern.compile("\"%s\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern NUMBER_CLAIM = Pattern.compile("\"%s\"\\s*:\\s*(\\d+)");
    private final byte[] secret;

    public JwtVerifier(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret must not be blank");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public JwtClaims verify(String token) {
        if (token == null || token.isBlank()) {
            throw new JwtValidationException("Missing token");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new JwtValidationException("Invalid token format");
        }

        String headerJson = decodeJson(parts[0]);
        String alg = extractStringClaim(headerJson, "alg");
        if (!"HS256".equals(alg)) {
            throw new JwtValidationException("Unsupported alg");
        }

        verifySignature(parts[0], parts[1], parts[2]);

        String payloadJson = decodeJson(parts[1]);
        UUID householdId = parseOptionalUuidClaim(payloadJson, "householdId");
        UUID userId = parseRequiredUuidClaim(payloadJson, "userId");
        long exp = parseExp(payloadJson);
        if (Instant.now().getEpochSecond() >= exp) {
            throw new JwtValidationException("Token expired");
        }

        return new JwtClaims(householdId, userId);
    }

    private String decodeJson(String part) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(part);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new JwtValidationException("Invalid token payload", ex);
        }
    }

    private void verifySignature(String header, String payload, String signaturePart) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] signed = mac.doFinal((header + "." + payload).getBytes(StandardCharsets.UTF_8));
            byte[] provided = Base64.getUrlDecoder().decode(signaturePart);
            if (!MessageDigest.isEqual(signed, provided)) {
                throw new JwtValidationException("Invalid signature");
            }
        } catch (JwtValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JwtValidationException("Signature verification failed", ex);
        }
    }

    private UUID parseRequiredUuidClaim(String payloadJson, String name) {
        String value = extractStringClaim(payloadJson, name);
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new JwtValidationException("Invalid claim: " + name, ex);
        }
    }

    private UUID parseOptionalUuidClaim(String payloadJson, String name) {
        String value = extractOptionalStringClaim(payloadJson, name);
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new JwtValidationException("Invalid claim: " + name, ex);
        }
    }

    private long parseExp(String payloadJson) {
        String value = extractNumberClaim(payloadJson, "exp");
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new JwtValidationException("Invalid exp", ex);
        }
    }

    private String extractStringClaim(String json, String name) {
        Pattern pattern = Pattern.compile(String.format(STRING_CLAIM.pattern(), Pattern.quote(name)));
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new JwtValidationException("Missing claim: " + name);
        }
        return matcher.group(1);
    }

    private String extractOptionalStringClaim(String json, String name) {
        Pattern pattern = Pattern.compile(String.format(STRING_CLAIM.pattern(), Pattern.quote(name)));
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    private String extractNumberClaim(String json, String name) {
        Pattern pattern = Pattern.compile(String.format(NUMBER_CLAIM.pattern(), Pattern.quote(name)));
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new JwtValidationException("Missing " + name);
        }
        return matcher.group(1);
    }
}
