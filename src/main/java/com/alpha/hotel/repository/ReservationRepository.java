package com.alpha.hotel.repository;

import com.alpha.hotel.model.enums.StatutReservation;
import com.alpha.hotel.model.enums.StatutSejour;
import com.alpha.hotel.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            select distinct r.chambre.id
            from Reservation r
            where r.statut <> :statutAnnule
              and r.dateArrivee < :dateDepart
              and r.dateDepart > :dateArrivee
            """)
    List<Long> findChambreIdsOccupees(
            @Param("dateArrivee") LocalDate dateArrivee,
            @Param("dateDepart") LocalDate dateDepart,
            @Param("statutAnnule") StatutReservation statutAnnule
    );

    @Query("""
            select distinct r.chambre.id
            from Reservation r
            where r.statut <> :statutAnnule
              and r.dateArrivee < :dateDepart
              and r.dateDepart > :dateArrivee
              and r.id <> :reservationId
            """)
    List<Long> findChambreIdsOccupeesEnExcluantReservation(
            @Param("dateArrivee") LocalDate dateArrivee,
            @Param("dateDepart") LocalDate dateDepart,
            @Param("statutAnnule") StatutReservation statutAnnule,
            @Param("reservationId") Long reservationId
    );

    List<Reservation> findByStatutSejourAndDateDepartBeforeAndStatut(
            StatutSejour statutSejour,
            LocalDate dateDepart,
            StatutReservation statut
    );

    List<Reservation> findByDateDepartAndStatut(LocalDate dateDepart, StatutReservation statut);
}
