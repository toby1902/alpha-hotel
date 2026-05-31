package com.alpha.hotel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReservationResponse {

    private Long id;
    private String clientNom;
    private String clientEmail;
    private String chambreNumero;
    private String typeChambre;
    private LocalDate dateArrivee;
    private LocalDate dateDepart;
    private BigDecimal montantTotal;
    private BigDecimal acompte;
    private String statut;
    private String statutSejour;
    private LocalDate dateCheckIn;
    private LocalDate dateCheckOut;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getChambreNumero() {
        return chambreNumero;
    }

    public void setChambreNumero(String chambreNumero) {
        this.chambreNumero = chambreNumero;
    }

    public String getTypeChambre() {
        return typeChambre;
    }

    public void setTypeChambre(String typeChambre) {
        this.typeChambre = typeChambre;
    }

    public LocalDate getDateArrivee() {
        return dateArrivee;
    }

    public void setDateArrivee(LocalDate dateArrivee) {
        this.dateArrivee = dateArrivee;
    }

    public LocalDate getDateDepart() {
        return dateDepart;
    }

    public void setDateDepart(LocalDate dateDepart) {
        this.dateDepart = dateDepart;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public BigDecimal getAcompte() {
        return acompte;
    }

    public void setAcompte(BigDecimal acompte) {
        this.acompte = acompte;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getStatutSejour() {
        return statutSejour;
    }

    public void setStatutSejour(String statutSejour) {
        this.statutSejour = statutSejour;
    }

    public LocalDate getDateCheckIn() {
        return dateCheckIn;
    }

    public void setDateCheckIn(LocalDate dateCheckIn) {
        this.dateCheckIn = dateCheckIn;
    }

    public LocalDate getDateCheckOut() {
        return dateCheckOut;
    }

    public void setDateCheckOut(LocalDate dateCheckOut) {
        this.dateCheckOut = dateCheckOut;
    }
}
