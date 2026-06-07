package br.com.effies.laboris.backend.domain.service;

import br.com.effies.laboris.backend.domain.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user){

        Instant now = Instant.now();
        Instant expiration = now.plus(8, ChronoUnit.HOURS);

        return Jwts.builder()
            .setIssuer("laboris-api")
            .setSubject(user.getUsername())
            .claim("role", user.getRole().name())
            .claim("passwordResetRequired", user.isPasswordResetRequired())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiration))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public String validateToken(String token){
        try{
            return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        } catch (Exception e){
            return "";
        }
    }

    public boolean isPasswordResetRequired(String token) {
        try {
            Boolean required = Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("passwordResetRequired", Boolean.class);
            return required != null && required;
        } catch (Exception e) {
            return false;
        }
    }

    private Key getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
