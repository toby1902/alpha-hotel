package com.alpha.hotel.model;

import com.alpha.hotel.model.enums.StatutReservation;
import com.alpha.hotel.model.enums.StatutSejour;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chambre_id")
    private Chambre chambre;

    @Column(nullable = false)
    private LocalDate dateArrivee;

    @Column(nullable = false)
    private LocalDate dateDepart;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal acompte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutReservation statut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutSejour statutSejour;

    private LocalDate dateCheckIn;

    private LocalDate dateCheckOut;

    private Boolean rappelDepartEnvoye = false;

    @OneToMany(mappedBy = "reservation")
    private List<Paiement> paiements = new ArrayList<>();

    @OneToOne(mappedBy = "reservation")
    private Facture facture;

    @PrePersist
    public void prePersist() {
        // Le montant total est calcule automatiquement si la reservation vient du formulaire client.
        if (montantTotal == null && chambre != null && dateArrivee != null && dateDepart != null) {
            long nuits = Math.max(1, ChronoUnit.DAYS.between(dateArrivee, dateDepart));
            montantTotal = chambre.getPrixParNuit().multiply(BigDecimal.valueOf(nuits));
        }
        // L'acompte represente 30 % du montant total, conformement a la regle metier.
        if (montantTotal != null) {
            acompte = montantTotal.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);
        }
        // Toute nouvelle reservation demarre en attente de validation reception.
        if (statut == null) {
            statut = StatutReservation.EN_ATTENTE;
        }
        if (statutSejour == null) {
            statutSejour = StatutSejour.NON_COMMENCE;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Chambre getChambre() {
        return chambre;
    }

    public void setChambre(Chambre chambre) {
        this.chambre = chambre;
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

    public StatutReservation getStatut() {
        return statut;
    }

    public void setStatut(StatutReservation statut) {
        this.statut = statut;
    }

    public List<Paiement> getPaiements() {
        return paiements;
    }

    public void setPaiements(List<Paiement> paiements) {
        this.paiements = paiements;
    }

    public StatutSejour getStatutSejour() {
        return statutSejour;
    }

    public void setStatutSejour(StatutSejour statutSejour) {
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

    public Facture getFacture() {
        return facture;
    }

    public void setFacture(Facture facture) {
        this.facture = facture;
    }

    public Boolean getRappelDepartEnvoye() {
        return rappelDepartEnvoye;
    }

    public void setRappelDepartEnvoye(Boolean rappelDepartEnvoye) {
        this.rappelDepartEnvoye = rappelDepartEnvoye;
    }
}
