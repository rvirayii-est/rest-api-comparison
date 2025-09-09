package com.example.secureapi.products;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class ProductsService {
  public List<Map<String, Object>> list() {
    return List.of(
      Map.of("id", "p1", "name", "Widget", "price", 4999),
      Map.of("id", "p2", "name", "Gadget", "price", 7999)
    );
  }
}
