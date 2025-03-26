package com.onion.backend.service;

import com.onion.backend.pojo.ArticleNotification;
import com.onion.backend.pojo.SendCommentNotification;
import com.onion.backend.pojo.WriteComment;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSender {

  private final RabbitTemplate rabbitTemplate;

  public RabbitMQSender(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void sendMessage(ArticleNotification articleNotification) {
    rabbitTemplate.convertAndSend("onion-notification", articleNotification);
  }

  public void sendMessage(WriteComment writeComment) {
    rabbitTemplate.convertAndSend("onion-notification", writeComment)
    ;
  }

  public void sendMessage(SendCommentNotification sendCommentNotification) {
    rabbitTemplate.convertAndSend("onion-notification", sendCommentNotification)
    ;
  }
}
