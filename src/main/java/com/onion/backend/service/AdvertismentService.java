package com.onion.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onion.backend.dto.AdHistoryResult;
import com.onion.backend.dto.AdViewHistoryResultDto;
import com.onion.backend.dto.AdvertismentDto;
import com.onion.backend.entity.*;
import com.onion.backend.exception.ResourcNotFoundException;
import com.onion.backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AdvertismentService {
  private static final String REDIS_KEY = "ad:";
  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper; // ObjectMapper 추가
  private final MongoTemplate mongoTemplate;

  AdvertismentRepository advertismentRepository;

  AdViewHistoryRepository adViewHistoryRepository;

  AdClickHistoryRepository adClickHistoryRepository;

  AdViewStatRepository adViewStatRepository;

  AdClickStatRepository adClickStatRepository;

  @Autowired
  public AdvertismentService(AdvertismentRepository advertismentRepository,
                             RedisTemplate<String, Object> redisTemplate,
                             ObjectMapper objectMapper,
                             AdViewHistoryRepository adViewHistoryRepository,
                             AdClickHistoryRepository adClickHistoryRepository,
                             MongoTemplate mongoTemplate,
                             AdViewStatRepository adViewStatRepository,
                             AdClickStatRepository adClickStatRepository) {
    this.advertismentRepository = advertismentRepository;
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.adViewHistoryRepository = adViewHistoryRepository;
    this.adClickHistoryRepository = adClickHistoryRepository;
    this.mongoTemplate = mongoTemplate;
    this.adViewStatRepository = adViewStatRepository;
    this.adClickStatRepository = adClickStatRepository;
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

  public Advertisment getAdById(Long adId, String clientIp, Boolean isTrueView) {
    this.insertAdHistory(adId, clientIp, isTrueView);
    // 광고 아이디 체크
    advertismentRepository.findById(adId).orElseThrow(() -> new ResourcNotFoundException("adId not found"));

    // Redis에서 광고 조회
    Object cachedAd = redisTemplate.opsForValue().get(REDIS_KEY + adId);
    if (cachedAd != null) {
      // LinkedHashMap → Advertisment 객체 변환
      return objectMapper.convertValue(cachedAd, Advertisment.class);
    }

    // Redis에 데이터가 없으면 DB에서 가져오고 캐싱
    Advertisment adFromDb = advertismentRepository.findById(adId).orElse(null);
    if (adFromDb != null) {
      redisTemplate.opsForValue().set(REDIS_KEY + adId, adFromDb, 1, TimeUnit.HOURS);
    }

    return adFromDb;
  }

  private void insertAdHistory(Long adId, String clientIp, Boolean isTrueView) {
    AdViewHistory adViewHistory = new AdViewHistory();
    adViewHistory.setAdId(adId);
    adViewHistory.setIp(clientIp);
    adViewHistory.setIsTrueView(isTrueView);
    adViewHistory.setCreatedAt(LocalDateTime.now());
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Object principal = authentication.getPrincipal();

    if(!principal.equals("anonymousUser")) {
      UserDetails userDetails = (UserDetails) principal;
      adViewHistory.setUsername(userDetails.getUsername());
    }

    adViewHistoryRepository.save(adViewHistory);
  }

  public void clickAd(Long adId, String clientIp) {
    AdClickHistory adClickHistory = new AdClickHistory();
    adClickHistory.setAdId(adId);
    adClickHistory.setIp(clientIp);
    adClickHistory.setCreatedAt(LocalDateTime.now());
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Object principal = authentication.getPrincipal();

    if(!principal.equals("anonymousUser")) {
      UserDetails userDetails = (UserDetails) principal;
      adClickHistory.setUsername(userDetails.getUsername());
    }

    adClickHistoryRepository.save(adClickHistory);
  }
  public List<AdViewHistoryResultDto> getAdViewHistoryGroupByAdId() {
    List<AdViewHistoryResultDto> usernameResult = this.getAdViewHistoryGroupByAdIdByUsername();
    List<AdViewHistoryResultDto> clientIpResult = this.getAdViewHistoryGroupByAdIdByClientIp();
    HashMap<Long, Long> totalResult = new HashMap<Long, Long>();
    for (AdViewHistoryResultDto item : usernameResult) {
      totalResult.put(item.getAdId(),item.getCount());
    }
    for (AdViewHistoryResultDto item : clientIpResult) {
      totalResult.merge(item.getAdId(), item.getCount(), Long::sum);
    }
    List<AdViewHistoryResultDto> resultDtoList = new ArrayList<>();
    for(Map.Entry<Long, Long> entry : totalResult.entrySet()){
      AdViewHistoryResultDto adViewHistoryResultDto = new AdViewHistoryResultDto();
      adViewHistoryResultDto.setAdId(entry.getKey());
      adViewHistoryResultDto.setCount(entry.getValue());
      resultDtoList.add(adViewHistoryResultDto);
    }
    return resultDtoList;
  }

  private  List<AdViewHistoryResultDto> getAdViewHistoryGroupByAdIdByUsername() {

    // 어제의 시작과 끝 시간 계산
    LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN).plusHours(9);
    LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).plusHours(9);

    // 어제 날짜에 해당하고, username 있는 문서 필터링
    MatchOperation matchStage = Aggregation.match(
        Criteria.where("createdAt").gte(startOfDay).lt(endOfDay).and("username").exists(true)
    );

    //adId로 그룹화하고 고유한 username 집합을 생성
    GroupOperation groupStage = Aggregation.group("adId").addToSet("username").as("uniqueUsername");

    // 고유한 username 잡합의 크기를 count로 계산
    ProjectionOperation projectionStage = Aggregation.project()
        .andExpression("size(uniqueUsername)").as("count").and("_id").as("adId");;

    // Aggregation 수행
    Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage, projectionStage);
    AggregationResults<AdViewHistoryResultDto> results = mongoTemplate.aggregate(aggregation, "adViewHistory", AdViewHistoryResultDto.class);
    return results.getMappedResults();
  }

  private  List<AdViewHistoryResultDto> getAdViewHistoryGroupByAdIdByClientIp() {

    // 어제의 시작과 끝 시간 계산
    LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN).plusHours(9);
    LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).plusHours(9);

    // 어제 날짜에 해당하고, username 있는 문서 필터링
    MatchOperation matchStage = Aggregation.match(
        Criteria.where("createdAt").gte(startOfDay).lt(endOfDay).and("username").exists(false)
    );

    //adId로 그룹화하고 고유한 username 집합을 생성
    GroupOperation groupStage = Aggregation.group("adId").addToSet("ip").as("uniqueIp");

    // 고유한 username 잡합의 크기를 count로 계산
    ProjectionOperation projectionStage = Aggregation.project()
        .andExpression("size(uniqueIp)").as("count").and("_id").as("adId");

    // Aggregation 수행
    Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage, projectionStage);
    AggregationResults<AdViewHistoryResultDto> results = mongoTemplate.aggregate(aggregation, "adViewHistory", AdViewHistoryResultDto.class);
    return results.getMappedResults();
  }

  public  void insertAdViewStat(List<AdViewHistoryResultDto> result){
    ArrayList<AdViewStat> adViewList = new ArrayList<>();
    for(AdViewHistoryResultDto item : result){
      AdViewStat adViewStat = new AdViewStat();
      adViewStat.setAdId(item.getAdId());
      adViewStat.setCount(item.getAdId());
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      adViewStat.setDt(LocalDateTime.now().minusDays(1).format(formatter));
      adViewList.add(adViewStat);
    }
    adViewStatRepository.saveAll(adViewList);
  }

  public List<AdHistoryResult> getAdClickHistoryGroupedByAdId() {
    List<AdHistoryResult> usernameResult = this.getAdClickHistoryGroupedByAdIdAndUsername();
    List<AdHistoryResult> clientipResult = this.getAdClickHistoryGroupedByAdIdAndClientip();
    HashMap<Long, Long> totalResult = new HashMap<>();
    for (AdHistoryResult item : usernameResult) {
      totalResult.put(item.getAdId(), item.getCount());
    }
    for (AdHistoryResult item : clientipResult) {
      totalResult.merge(item.getAdId(), item.getCount(), Long::sum);
    }

    List<AdHistoryResult> resultList = new ArrayList<>();
    for (Map.Entry<Long, Long> entry : totalResult.entrySet()) {
      AdHistoryResult result = new AdHistoryResult();
      result.setAdId(entry.getKey());
      result.setCount(entry.getValue());
      resultList.add(result);
    }
    return resultList;
  }

  private List<AdHistoryResult> getAdClickHistoryGroupedByAdIdAndUsername() {
    // 어제의 시작과 끝 시간 계산
    LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN).plusHours(9);
    LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).plusHours(9);

    // Match 단계: 어제 날짜에 해당하고, username이 있는 문서 필터링
    MatchOperation matchStage = Aggregation.match(
        Criteria.where("createdAt").gte(startOfDay).lt(endOfDay)
            .and("username").exists(true)
    );

    // Group 단계: adId로 그룹화하고 고유한 username 집합을 생성
    GroupOperation groupStage = Aggregation.group("adId")
        .addToSet("username").as("uniqueUsernames");

    // Projection 단계: 고유한 username 집합의 크기를 count로 계산
    ProjectionOperation projectStage = Aggregation.project()
        .andExpression("_id").as("adId")
        .andExpression("size(uniqueUsernames)").as("count");

    // Aggregation 수행
    Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage, projectStage);
    AggregationResults<AdHistoryResult> results = mongoTemplate.aggregate(aggregation, "adClickHistory", AdHistoryResult.class);

    return results.getMappedResults();
  }

  private List<AdHistoryResult> getAdClickHistoryGroupedByAdIdAndClientip() {
    // 어제의 시작과 끝 시간 계산
    LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN).plusHours(9);
    LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).plusHours(9);

    // Match 단계: 어제 날짜에 해당하고, username이 있는 문서 필터링
    MatchOperation matchStage = Aggregation.match(
        Criteria.where("createdAt").gte(startOfDay).lt(endOfDay)
            .and("username").exists(false)
    );

    // Group 단계: adId로 그룹화하고 고유한 username 집합을 생성
    GroupOperation groupStage = Aggregation.group("adId")
        .addToSet("Ip").as("uniqueIp");

    // Projection 단계: 고유한 username 집합의 크기를 count로 계산
    ProjectionOperation projectStage = Aggregation.project()
        .andExpression("_id").as("adId")
        .andExpression("size(uniqueIp)").as("count");

    // Aggregation 수행
    Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage, projectStage);
    AggregationResults<AdHistoryResult> results = mongoTemplate.aggregate(aggregation, "adClickHistory", AdHistoryResult.class);

    return results.getMappedResults();
  }

  public void insertAdClickStat(List<AdHistoryResult> result) {
    ArrayList<AdClickStat> arrayList = new ArrayList<>();
    for (AdHistoryResult item : result) {
      AdClickStat adClickStat = new AdClickStat();
      adClickStat.setAdId(item.getAdId());
      adClickStat.setCount(item.getCount());
      // yyyy-MM-dd 형식의 DateTimeFormatter 생성
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      // LocalDateTime을 yyyy-MM-dd 형식의 문자열로 변환
      String formattedDate = LocalDateTime.now().minusDays(1).format(formatter);
      adClickStat.setDt(formattedDate);
      arrayList.add(adClickStat);
    }
    adClickStatRepository.saveAll(arrayList);
  }
}
