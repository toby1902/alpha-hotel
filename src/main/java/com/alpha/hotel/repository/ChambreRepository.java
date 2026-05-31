package com.alpha.hotel.repository;

import com.alpha.hotel.model.Chambre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChambreRepository extends JpaRepository<Chambre, Long> {
    List<Chambre> findByDisponibleTrueOrderByPrixParNuitAsc();
    Optional<Chambre> findByNumero(String numero);
}
