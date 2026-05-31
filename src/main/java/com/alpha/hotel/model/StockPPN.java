package com.alpha.hotel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "stocks_ppn")
public class StockPPN {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nomProduit;

    @Column(nullable = false)
    private Integer quantite;

    @Column(nullable = false, length = 30)
    private String unite;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(nullable = false)
    private Boolean alerteActive = false;

    @Column(length = 255)
    private String messageAlerte;

    public StockPPN() {
    }

    public StockPPN(String nomProduit, Integer quantite, String unite, BigDecimal prixUnitaire) {
        this.nomProduit = nomProduit;
        this.quantite = quantite;
        this.unite = unite;
        this.prixUnitaire = prixUnitaire;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    public String getUnite() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public Boolean getAlerteActive() {
        return alerteActive;
    }

    public void setAlerteActive(Boolean alerteActive) {
        this.alerteActive = alerteActive;
    }

    public String getMessageAlerte() {
        return messageAlerte;
    }

    public void setMessageAlerte(String messageAlerte) {
        this.messageAlerte = messageAlerte;
    }
}
