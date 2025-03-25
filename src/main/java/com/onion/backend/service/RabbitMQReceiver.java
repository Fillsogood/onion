package com.onion.backend.service;

import com.onion.backend.entity.ArticleNotification;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;

@Service
public class RabbitMQReceiver {

  @RabbitListener(queues = "onion-notification")
  public void receive(ArticleNotification articleNotification) {
    Timer timer = new Timer();

    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        System.out.println("Received Message: "+articleNotification.toString());
      }
    },5000);

  }
}
