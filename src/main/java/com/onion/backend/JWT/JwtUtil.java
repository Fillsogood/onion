package com.onion.backend.JWT;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class  JwtUtil {


  private String jwtSecret ="ZL7mGk2aIm4oc+WoPYfyTyNqAkhQ5gDFIlfE8NYcl8Y=";

  private int jwtExpirationMs = 3600000;

  // JWT 토큰 생성
  public String generateJwtToken(String username) {
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
        .signWith(SignatureAlgorithm.HS256, jwtSecret)
        .compact();
  }

  // JWT 토큰에서 사용자명 추출
  public String getUsernameFromJwtToken(String token) {
    return Jwts.parser()
        .setSigningKey(jwtSecret)
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  // JWT 토큰 검증
  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parser()
          .setSigningKey(jwtSecret)
          .parseClaimsJws(authToken);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
