package com.onion.backend.service;

import com.onion.backend.pojo.ArticleNotification;
import com.onion.backend.pojo.SendCommentNotification;
import com.onion.backend.pojo.WriteComment;
import com.onion.backend.repository.CommentRepository;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;


import java.util.*;

@RabbitListener(queues = "onion-notification")
@Service
public class RabbitMQReceiver {

  private final CommentService commentService;

  public RabbitMQReceiver(CommentService commentService) {
    this.commentService = commentService;
  }

  @RabbitHandler
  public void receive(ArticleNotification articleNotification) {
    Timer timer = new Timer();

    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        System.out.println("Received Message: "+articleNotification.toString());
      }
    },5000);

  }

  @RabbitHandler
  public void receive(WriteComment comment) {
    System.out.println("댓글 작성 감지: " + comment.toString());
    commentService.sendCommentNotification(comment);
  }

  @RabbitHandler
  public void receive(SendCommentNotification sendCommentNotification) {
    System.out.println("댓글 작성 감지: " + sendCommentNotification.toString());
  }


}
