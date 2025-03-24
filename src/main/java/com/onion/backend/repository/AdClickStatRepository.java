package com.onion.backend.repository;

import com.onion.backend.entity.AdClickStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdClickStatRepository extends JpaRepository<AdClickStat, String> {

}
