package com.example.secureapi.orders;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class OrdersService {
  public List<Map<String, Object>> listAll() { return List.of(Map.of("id","o1","userId","u1","productId","p1","status","PAID")); }
  public List<Map<String, Object>> listByUser(String userId) { return List.of(Map.of("id","oX","userId",userId,"productId","p2","status","PAID")); }
}
