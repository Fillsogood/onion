package com.onion.backend.exception;

public class ResourcNotFoundException extends RuntimeException {
   public ResourcNotFoundException(String message) {
     super(message);
   }
}
