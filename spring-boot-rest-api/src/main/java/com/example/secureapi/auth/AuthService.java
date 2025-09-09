package com.example.secureapi.auth;

import com.example.secureapi.tokens.RefreshTokenService;
import com.example.secureapi.users.UserEntity;
import com.example.secureapi.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;

@Service @RequiredArgsConstructor
public class AuthService {
  private final UserService users;
  private final RefreshTokenService tokens;
  private final JwtEncoder jwtEncoder;

  @Value("${app.access-token-ttl-seconds}") private long accessTtl;
  @Value("${app.refresh-token-ttl-seconds}") private long refreshTtl;

  private static String sha256Hex(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] d = md.digest(s.getBytes());
      StringBuilder sb = new StringBuilder();
      for (byte b : d) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  public record Tokens(String access, String refresh) {}

  public Tokens login(UserEntity user) {
    Instant now = Instant.now();
    JwtClaimsSet access = JwtClaimsSet.builder()
      .issuer("secure-api")
      .issuedAt(now)
      .expiresAt(now.plusSeconds(accessTtl))
      .subject(user.getId().toString())
      .claim("email", user.getEmail())
      .claim("roles", user.getRoles())
      .build();
    String accessJwt = jwtEncoder.encode(JwtEncoderParameters.from(access)).getTokenValue();

    JwtClaimsSet refresh = JwtClaimsSet.builder()
      .issuer("secure-api")
      .issuedAt(now)
      .expiresAt(now.plusSeconds(refreshTtl))
      .subject(user.getId().toString())
      .build();
    String refreshJwt = jwtEncoder.encode(JwtEncoderParameters.from(refresh)).getTokenValue();

    tokens.store(user.getId(), sha256Hex(refreshJwt));
    return new Tokens(accessJwt, refreshJwt);
  }

  public Tokens rotate(UserEntity user, String oldRefresh) {
    tokens.revoke(sha256Hex(oldRefresh));
    return login(user);
  }
}
