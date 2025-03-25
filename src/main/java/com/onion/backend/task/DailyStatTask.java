package com.onion.backend.task;

import com.onion.backend.dto.AdHistoryResult;
import com.onion.backend.dto.AdViewHistoryResultDto;
import com.onion.backend.service.AdvertismentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DailyStatTask {
  private final AdvertismentService advertismentService;

  @Autowired
  public DailyStatTask(AdvertismentService advertismentService) {
    this.advertismentService = advertismentService;
  }

  @Scheduled(cron = "0 0 0 * * ?")
  public void insertAdViewStatAtMidnight() {
    List<AdViewHistoryResultDto> viewResult = advertismentService.getAdViewHistoryGroupByAdId();
    advertismentService.insertAdViewStat(viewResult);
    List<AdHistoryResult> clicksResult = advertismentService.getAdClickHistoryGroupedByAdId();
    advertismentService.insertAdClickStat(clicksResult);
  }
}
