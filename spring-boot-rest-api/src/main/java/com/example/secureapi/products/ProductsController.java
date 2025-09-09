package com.example.secureapi.products;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductsController {
  private final ProductsService svc;
  @GetMapping
  public List<Map<String, Object>> list() { return svc.list(); }
}
