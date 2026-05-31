package com.alpha.hotel.repository;

import com.alpha.hotel.model.StockPPN;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockPPNRepository extends JpaRepository<StockPPN, Long> {
    List<StockPPN> findByQuantiteLessThanEqualOrderByQuantiteAsc(Integer seuil);
}
