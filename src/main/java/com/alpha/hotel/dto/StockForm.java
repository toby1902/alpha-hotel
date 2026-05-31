package com.alpha.hotel.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class StockForm {

    @NotBlank(message = "Le nom du produit est obligatoire.")
    @Size(min = 2, max = 100, message = "Le nom du produit doit contenir entre 2 et 100 caracteres.")
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ0-9'()\\- ,.]+$", message = "Le nom du produit contient des caracteres non autorises.")
    private String nomProduit;

    @NotNull(message = "La quantite est obligatoire.")
    @Min(value = 0, message = "La quantite doit etre positive ou nulle.")
    private Integer quantite;

    @NotBlank(message = "L'unite est obligatoire.")
    @Size(min = 2, max = 30, message = "L'unite doit contenir entre 2 et 30 caracteres.")
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ ]+$", message = "L'unite ne doit contenir que des lettres et espaces.")
    private String unite;

    @NotNull(message = "Le prix unitaire est obligatoire.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix unitaire doit etre superieur a zero.")
    private BigDecimal prixUnitaire;

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
}
