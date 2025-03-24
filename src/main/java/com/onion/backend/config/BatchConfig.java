package com.onion.backend.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    this.jobRepository = jobRepository;
    this.transactionManager = transactionManager;
  }

  @Bean
  public Job exampleJob() {
    return new JobBuilder("exampleJob", jobRepository)
        .start(exampleStep())
        .build();
  }

  @Bean
  public Step exampleStep() {
    return new StepBuilder("exampleStep", jobRepository)
        .tasklet(exampleTasklet(), transactionManager)
        .build();
  }

  @Bean
  public Tasklet exampleTasklet() {
    return ((contribution, chunkContext) -> {
      System.out.println("Hello, Spring Batch");
      return RepeatStatus.FINISHED;
    });
  }
}
