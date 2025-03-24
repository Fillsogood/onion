package com.onion.backend.repository;

import com.onion.backend.entity.AdViewHistory;
import com.onion.backend.entity.AdViewStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdViewStatRepository extends JpaRepository<AdViewStat, String> {

}
