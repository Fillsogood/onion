package com.onion.backend.dto;


import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdvertismentDto {
   String title;
   String content;
   Boolean isDeleted;
   Boolean isVisble;
   LocalDateTime startDate;
   LocalDateTime endDate;
   Integer clickCount;
   Integer viewCount;

}
