package com.example.secureapi.users;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class UserService {
  private final UserRepository repo;
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

  public UserEntity register(String email, String password) {
    UserEntity u = UserEntity.builder()
      .email(email)
      .passwordHash(encoder.encode(password))
      .roles(List.of("USER"))
      .build();
    return repo.save(u);
  }

  public Optional<UserEntity> findByEmail(String email) { return repo.findByEmail(email); }
  public Optional<UserEntity> findById(UUID id) { return repo.findById(id); }
  public void makeAdmin(UUID id) { repo.findById(id).ifPresent(u -> { u.setRoles(List.of("USER","ADMIN")); repo.save(u); }); }
  public boolean passwordMatches(String raw, String hashed) { return encoder.matches(raw, hashed); }
}
