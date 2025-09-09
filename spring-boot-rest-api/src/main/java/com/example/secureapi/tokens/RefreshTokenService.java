package com.example.secureapi.tokens;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service @RequiredArgsConstructor
public class RefreshTokenService {
  private final RefreshTokenRepository repo;

  public void store(java.util.UUID userId, String tokenHash) {
    repo.save(RefreshTokenEntity.builder().userId(userId).tokenHash(tokenHash).build());
  }

  public boolean isValid(String tokenHash) {
    return repo.findByTokenHashAndRevokedAtIsNull(tokenHash).isPresent();
  }

  public Optional<RefreshTokenEntity> findActive(String tokenHash) {
    return repo.findByTokenHashAndRevokedAtIsNull(tokenHash);
  }

  public void revoke(String tokenHash) {
    repo.findByTokenHashAndRevokedAtIsNull(tokenHash).ifPresent(rt -> { rt.setRevokedAt(OffsetDateTime.now()); repo.save(rt); });
  }
}
