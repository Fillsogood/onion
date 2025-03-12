package com.onion.backend.repository;

import com.onion.backend.entity.Advertisment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertismentRepository extends JpaRepository<Advertisment, Long> {

}
