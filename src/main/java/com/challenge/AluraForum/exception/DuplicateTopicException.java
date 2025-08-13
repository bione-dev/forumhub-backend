package com.challenge.AluraForum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateTopicException extends RuntimeException {
  public DuplicateTopicException(String message) {
    super(message);
  }
}
