package com.onion.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onion.backend.dto.AdvertismentDto;
import com.onion.backend.entity.Advertisment;
import com.onion.backend.exception.ResourcNotFoundException;
import com.onion.backend.repository.AdvertismentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AdvertismentService {
  private static final String REDIS_KEY = "ad:";
  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper; // ObjectMapper 추가

  AdvertismentRepository advertismentRepository;

  @Autowired
  public AdvertismentService(AdvertismentRepository advertismentRepository, RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
    this.advertismentRepository = advertismentRepository;
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public Advertisment saveAdvertisment(AdvertismentDto advertismentDto) {
    Advertisment advertisment = new Advertisment();
    advertisment.setTitle(advertismentDto.getTitle());
    advertisment.setContent(advertismentDto.getContent());
    advertisment.setIsDeleted(advertismentDto.getIsDeleted());
    advertisment.setIsVisble(advertismentDto.getIsVisble());
    advertisment.setStartDate(advertismentDto.getStartDate());
    advertisment.setEndDate(advertismentDto.getEndDate());
    advertisment.setViewCount(advertismentDto.getViewCount());
    advertisment.setClickCount(advertismentDto.getClickCount());
    advertismentRepository.save(advertisment);
    // Redis에 광고 캐싱 (TTL 1시간 설정)
    redisTemplate.opsForValue().set(REDIS_KEY + advertisment.getId(), advertisment, 1, TimeUnit.HOURS);

    return advertisment;
  }

  public List<Advertisment> getAdList() {
    return advertismentRepository.findAll();
  }

  public Advertisment getAdById(Long id) {
    // 광고 아이디 체크
    advertismentRepository.findById(id).orElseThrow(() -> new ResourcNotFoundException("adId not found"));

    // Redis에서 광고 조회
    Object cachedAd = redisTemplate.opsForValue().get(REDIS_KEY + id);
    if (cachedAd != null) {
      // LinkedHashMap → Advertisment 객체 변환
      return objectMapper.convertValue(cachedAd, Advertisment.class);
    }

    // Redis에 데이터가 없으면 DB에서 가져오고 캐싱
    Advertisment adFromDb = advertismentRepository.findById(id).orElse(null);
    if (adFromDb != null) {
      redisTemplate.opsForValue().set(REDIS_KEY + id, adFromDb, 1, TimeUnit.HOURS);
    }

    return adFromDb;
  }
}
