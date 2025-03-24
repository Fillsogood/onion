package com.onion.backend.repository;

import com.onion.backend.entity.AdClickHistory;
import com.onion.backend.entity.AdViewHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdClickHistoryRepository extends MongoRepository<AdClickHistory, String> {

}
