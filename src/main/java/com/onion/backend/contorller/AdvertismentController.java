package com.onion.backend.contorller;

import com.onion.backend.dto.AdvertismentDto;
import com.onion.backend.entity.Advertisment;
import com.onion.backend.service.AdvertismentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ads")
public class AdvertismentController {
  private final AdvertismentService advertismentService;


  @Autowired
  public AdvertismentController(AdvertismentService advertismentService) {
    this.advertismentService = advertismentService;
  }

  @PostMapping
  public ResponseEntity<Advertisment> createAdvertisment(@RequestBody AdvertismentDto advertismentDto) {
    Advertisment advertisment = advertismentService.saveAdvertisment(advertismentDto);
    return ResponseEntity.ok(advertisment);
  }

  @GetMapping
  public ResponseEntity<List<Advertisment>> getAllAdvertisments() {
    List<Advertisment> advertismentList = advertismentService.getAdList();
    return ResponseEntity.ok(advertismentList);
  }

  @GetMapping("/{adId}")
  public ResponseEntity<Advertisment> getAdvertisment(@PathVariable Long adId) {
    Advertisment advertisment = advertismentService.getAdById(adId);
    return ResponseEntity.ok(advertisment);
  }
}
