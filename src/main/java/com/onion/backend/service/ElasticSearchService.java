package com.onion.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onion.backend.entity.Article;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ElasticSearchService {
  private final WebClient webClient;
  private final ObjectMapper objectMapper;

  public ElasticSearchService(WebClient webClient, ObjectMapper objectMapper) {
    this.webClient = webClient;
    this.objectMapper = objectMapper;
  }

  // 게시글을 Elasticsearch에 저장하는 메서드
  public Mono<String> indexArticle(Article article) {
    return Mono.defer(() -> {
      try {
        // Article 객체를 JSON 문자열로 변환
        String articleJson = objectMapper.writeValueAsString(article);

        // Elasticsearch에 문서 저장 (비동기 방식 유지)
        return this.indexArticleDocument(article.getId().toString(), articleJson);
      } catch (JsonProcessingException e) {
        // JSON 변환 예외 발생 시 Mono.error로 반환
        return Mono.error(new RuntimeException("Failed to convert article to JSON", e));
      }
    });
  }

  // 문서 추가 (Indexing)
  public Mono<String> indexArticleDocument(String id, String document) {
    return webClient.put()
        .uri("/article/_doc/{id}", id)  // 🔹 인덱스 이름이 `articles`인지 확인 필요\
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .bodyValue(document)
        .retrieve()
        .bodyToMono(String.class);
  }

  // 문서 검색 (Match Query)
  public Mono<List<Long>> articleSearch(String keyword) {
    String query = """
        {
          "_source": "false",
          "size": 10,
          "query": {
            "match": {
              "content": "%s"
            }
          },
          "fields": ["_id"]
        }
        """.formatted(keyword);

    return webClient.post()
        .uri("/article/_search")
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .bodyValue(query)
        .retrieve()
        .bodyToMono(String.class)
        .map(this::extractArticleIds);
  }

  private List<Long> extractArticleIds(String jsonResponse) {
    try {
      Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, new TypeReference<>() {
      });
      List<Long> articleIds = new ArrayList<>();
      Map<String, Object> hits = (Map<String, Object>) responseMap.get("hits");

      if (hits != null) {
        List<Map<String, Object>> hitsList = (List<Map<String, Object>>) hits.get("hits");
        if (hitsList != null) {
          for (Map<String, Object> hit : hitsList) {
            try {
              // 🔹 `_id`를 String → Long 변환 후 저장
              articleIds.add(Long.parseLong((String) hit.get("_id")));
            } catch (NumberFormatException e) {
              System.err.println("Invalid ID format: " + hit.get("_id"));
            }
          }
        }
      }
      return articleIds;
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to parse Elasticsearch response", e);
    }
  }
}
