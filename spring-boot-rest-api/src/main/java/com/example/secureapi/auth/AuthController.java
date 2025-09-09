package com.example.secureapi.auth;

import com.example.secureapi.auth.dto.LoginRequest;
import com.example.secureapi.auth.dto.RegisterRequest;
import com.example.secureapi.auth.dto.TokenResponse;
import com.example.secureapi.tokens.RefreshTokenEntity;
import com.example.secureapi.tokens.RefreshTokenService;
import com.example.secureapi.users.UserEntity;
import com.example.secureapi.users.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private final UserService users;
  private final AuthService auth;
  private final RefreshTokenService tokens;
  private final JwtDecoder jwtDecoder;

  @Value("${app.refresh-cookie-name}") private String refreshCookieName;
  @Value("${app.cookie-domain}") private String cookieDomain;
  @Value("${app.cookie-secure}") private boolean cookieSecure;
  @Value("${app.refresh-token-ttl-seconds}") private int refreshTtl;
  @Value("${app.cookie-samesite}") private String sameSite;

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest body) {
    users.register(body.email(), body.password());
    return ResponseEntity.ok().body(java.util.Map.of("message", "registered"));
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest body, HttpServletResponse res) {
    UserEntity user = users.findByEmail(body.email()).orElse(null);
    if (user == null || !users.passwordMatches(body.password(), user.getPasswordHash())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    var tokensPair = auth.login(user);
    setRefreshCookie(res, tokensPair.refresh());
    return ResponseEntity.ok(new TokenResponse(tokensPair.access()));
  }

  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(HttpServletRequest req, HttpServletResponse res) {
    String cookieVal = readRefreshCookie(req);
    if (cookieVal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

    // verify signature/exp
    try { jwtDecoder.decode(cookieVal); } catch (JwtException e) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }

    var tokenHash = sha256(cookieVal);
    Optional<RefreshTokenEntity> rec = tokens.findActive(tokenHash);
    if (rec.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

    var user = users.findById(rec.get().getUserId()).orElse(null);
    if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

    var newTokens = auth.rotate(user, cookieVal);
    setRefreshCookie(res, newTokens.refresh());
    return ResponseEntity.ok(new TokenResponse(newTokens.access()));
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest req, HttpServletResponse res) {
    String cookieVal = readRefreshCookie(req);
    if (cookieVal != null) tokens.revoke(sha256(cookieVal));
    clearRefreshCookie(res);
    return ResponseEntity.ok(java.util.Map.of("message", "logged out"));
  }

  private void setRefreshCookie(HttpServletResponse res, String value) {
    Cookie c = new Cookie(refreshCookieName, value);
    c.setHttpOnly(true);
    c.setSecure(cookieSecure);
    c.setPath("/auth");
    c.setMaxAge(refreshTtl);
    if (cookieDomain != null && !cookieDomain.isBlank()) c.setDomain(cookieDomain);
    res.addHeader("Set-Cookie", cookieHeader(c, sameSite));
  }

  private void clearRefreshCookie(HttpServletResponse res) {
    Cookie c = new Cookie(refreshCookieName, "");
    c.setPath("/auth");
    c.setMaxAge(0);
    if (cookieDomain != null && !cookieDomain.isBlank()) c.setDomain(cookieDomain);
    res.addHeader("Set-Cookie", cookieHeader(c, sameSite));
  }

  private static String cookieHeader(Cookie c, String sameSite) {
    String base = String.format("%s=%s; Max-Age=%d; Path=%s", c.getName(), c.getValue(), c.getMaxAge(), c.getPath());
    String domain = c.getDomain() != null ? "; Domain=" + c.getDomain() : "";
    String secure = c.getSecure() ? "; Secure" : "";
    String httpOnly = "; HttpOnly";
    String ss = "; SameSite=" + (sameSite == null ? "Strict" : sameSite.substring(0,1).toUpperCase()+sameSite.substring(1).toLowerCase());
    return base + domain + secure + httpOnly + ss;
  }

  private static String sha256(String s) {
    try {
      java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
      byte[] d = md.digest(s.getBytes());
      StringBuilder sb = new StringBuilder();
      for (byte b : d) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  private String readRefreshCookie(HttpServletRequest req) {
    if (req.getCookies() == null) return null;
    for (Cookie c : req.getCookies()) if (c.getName().equals(refreshCookieName)) return c.getValue();
    return null;
  }
}
