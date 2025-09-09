package com.example.secureapi.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {
  private final OrdersService svc;

  @GetMapping
  // requires ROLE_ADMIN via SecurityConfig
  public List<Map<String, Object>> all() { return svc.listAll(); }

  @GetMapping("/me")
  public List<Map<String, Object>> mine(@AuthenticationPrincipal Jwt jwt) {
    String userId = jwt.getSubject();
    return svc.listByUser(userId);
  }
}
