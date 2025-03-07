package com.onion.backend.handler;

import com.onion.backend.exception.ForbiddenException;
import com.onion.backend.exception.RateLimitException;
import com.onion.backend.exception.ResourcNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  //404Not Found
  @ExceptionHandler(ResourcNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleResourceNotFoundException(ResourcNotFoundException e) {
    return e.getMessage();
  }

  //5분 이상 예외 처리
  @ExceptionHandler(RateLimitException.class)
  @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
  public String handleResourceLimitException(RateLimitException e) {
    return e.getMessage();
  }

  //권한
  @ExceptionHandler(ForbiddenException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public String handleResourceFORBIDDENException(ForbiddenException e) {
    return e.getMessage();
  }
}
