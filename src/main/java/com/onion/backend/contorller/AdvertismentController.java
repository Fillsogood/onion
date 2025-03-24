package com.onion.backend.contorller;

import com.onion.backend.dto.AdViewHistoryResultDto;
import com.onion.backend.dto.AdvertismentDto;
import com.onion.backend.entity.Advertisment;
import com.onion.backend.service.AdvertismentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AdvertismentController {
  private final AdvertismentService advertismentService;


  @Autowired
  public AdvertismentController(AdvertismentService advertismentService) {
    this.advertismentService = advertismentService;
  }

  @PostMapping("/admin/ads")
  public ResponseEntity<Advertisment> createAdvertisment(@RequestBody AdvertismentDto advertismentDto) {
    Advertisment advertisment = advertismentService.saveAdvertisment(advertismentDto);
    return ResponseEntity.ok(advertisment);
  }

  @GetMapping("/ads")
  public ResponseEntity<List<Advertisment>> getAllAdvertisments() {
    List<Advertisment> advertismentList = advertismentService.getAdList();
    return ResponseEntity.ok(advertismentList);
  }

  @GetMapping("/ads/{adId}")
  public ResponseEntity<Advertisment> getAdvertisment(@PathVariable Long adId, HttpServletRequest request, @RequestParam(required = false) Boolean isTrueView) {
    String ClientIp = request.getRemoteAddr();
    Advertisment advertisment = advertismentService.getAdById(adId,ClientIp, isTrueView != null && isTrueView);
    return ResponseEntity.ok(advertisment);
  }

  @PostMapping("/ads/{adId}")
  public ResponseEntity<String> ClickAd(@PathVariable Long adId, HttpServletRequest request) {
    String ClientIp = request.getRemoteAddr();
    advertismentService.clickAd(adId,ClientIp);
    return ResponseEntity.ok("click");
  }

  @GetMapping("/ads/history")
  public ResponseEntity<List<AdViewHistoryResultDto>> getHistory() {

    List<AdViewHistoryResultDto> result = advertismentService.getAdViewHistoryGroupByAdId();
    advertismentService.insertAdViewStat(result);
    return ResponseEntity.ok(result);
  }
}
