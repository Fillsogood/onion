package com.onion.backend.service;

import com.onion.backend.entity.ArticleNotification;
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
}
