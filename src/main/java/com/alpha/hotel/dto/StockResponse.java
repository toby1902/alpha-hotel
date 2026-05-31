package com.alpha.hotel.dto;

import java.math.BigDecimal;

public class StockResponse {

    private Long id;
    private String nomProduit;
    private Integer quantite;
    private String unite;
    private BigDecimal prixUnitaire;
    private Boolean alerteActive;
    private String messageAlerte;

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
