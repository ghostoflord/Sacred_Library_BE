package com.ghost.sacred_library.security;

import com.ghost.sacred_library.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret:}") String encodedSecret,
                      @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {
        if (encodedSecret.isBlank()) throw new IllegalStateException("JWT_SECRET must be set to a Base64-encoded secret of at least 32 bytes.");
        byte[] bytes;
        try { bytes = Decoders.BASE64.decode(encodedSecret); }
        catch (RuntimeException ex) { throw new IllegalStateException("JWT_SECRET must be valid Base64."); }
        if (bytes.length < 32) throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes for HS256.");
        if (expirationMs <= 0) throw new IllegalStateException("JWT_EXPIRATION_MS must be greater than zero.");
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMs = expirationMs;
    }

    public String createToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(user.getUsername()).claim("uid", user.getId())
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key).compact();
    }
    public Claims parseAndValidate(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
    public long getExpirationMs() { return expirationMs; }
}
