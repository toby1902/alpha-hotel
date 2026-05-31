package com.alpha.hotel.dto;

import java.math.BigDecimal;

public class DashboardStats {

    private long totalReservations;
    private long reservationsEnAttente;
    private long reservationsConfirmees;
    private long reservationsAnnulees;
    private long stocksCritiques;
    private BigDecimal montantEncaisse = BigDecimal.ZERO;

    public long getTotalReservations() {
        return totalReservations;
    }

    public void setTotalReservations(long totalReservations) {
        this.totalReservations = totalReservations;
    }

    public long getReservationsEnAttente() {
        return reservationsEnAttente;
    }

    public void setReservationsEnAttente(long reservationsEnAttente) {
        this.reservationsEnAttente = reservationsEnAttente;
    }

    public long getReservationsConfirmees() {
        return reservationsConfirmees;
    }

    public void setReservationsConfirmees(long reservationsConfirmees) {
        this.reservationsConfirmees = reservationsConfirmees;
    }

    public long getReservationsAnnulees() {
        return reservationsAnnulees;
    }

    public void setReservationsAnnulees(long reservationsAnnulees) {
        this.reservationsAnnulees = reservationsAnnulees;
    }

    public long getStocksCritiques() {
        return stocksCritiques;
    }

    public void setStocksCritiques(long stocksCritiques) {
        this.stocksCritiques = stocksCritiques;
    }

    public BigDecimal getMontantEncaisse() {
        return montantEncaisse;
    }

    public void setMontantEncaisse(BigDecimal montantEncaisse) {
        this.montantEncaisse = montantEncaisse;
    }
}
