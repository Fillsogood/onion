package com.onion.backend.handler;

import com.onion.backend.exception.RateLimitException;
import com.onion.backend.exception.ResourcNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ResourcNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleResourceNotFoundException(ResourcNotFoundException e) {
    return e.getMessage();
  }

  @ExceptionHandler(RateLimitException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public String handleResourceFORBIDDENException(RateLimitException e) {
    return e.getMessage();
  }
}
