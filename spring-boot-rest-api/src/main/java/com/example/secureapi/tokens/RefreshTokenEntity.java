package com.example.secureapi.tokens;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "refresh_tokens", indexes = {
  @Index(name = "ix_token_hash", columnList = "token_hash", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshTokenEntity {
  @Id @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, name = "user_id")
  private UUID userId;

  @Column(nullable = false, name = "token_hash", unique = true)
  private String tokenHash;

  @Column(nullable = false)
  private OffsetDateTime createdAt = OffsetDateTime.now();

  private OffsetDateTime revokedAt;
}
