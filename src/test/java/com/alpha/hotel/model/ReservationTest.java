package com.alpha.hotel.model;

import com.alpha.hotel.model.enums.StatutReservation;
import com.alpha.hotel.model.enums.StatutSejour;
import com.alpha.hotel.model.enums.TypeChambre;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ReservationTest {

    @Test
    void prePersistDoitCalculerAcompteEtStatutInitial() {
        Chambre chambre = new Chambre("101", TypeChambre.SIMPLE, new BigDecimal("100.00"), 1, true, "Test");

        Reservation reservation = new Reservation();
        reservation.setChambre(chambre);
        reservation.setDateArrivee(LocalDate.of(2026, 4, 22));
        reservation.setDateDepart(LocalDate.of(2026, 4, 25));

        reservation.prePersist();

        assertEquals(new BigDecimal("300.00"), reservation.getMontantTotal());
        assertEquals(new BigDecimal("90.00"), reservation.getAcompte());
        assertEquals(StatutReservation.EN_ATTENTE, reservation.getStatut());
        assertEquals(StatutSejour.NON_COMMENCE, reservation.getStatutSejour());
        assertFalse(Boolean.TRUE.equals(reservation.getRappelDepartEnvoye()));
    }
}
