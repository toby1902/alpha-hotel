package com.alpha.hotel.repository;

import com.alpha.hotel.model.Facture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FactureRepository extends JpaRepository<Facture, Long> {
    Optional<Facture> findByReservationId(Long reservationId);
}
