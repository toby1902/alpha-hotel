package com.alpha.hotel.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class PaiementForm {

    @NotNull(message = "Le montant du paiement est obligatoire.")
    @DecimalMin(value = "0.01", message = "Le montant doit etre superieur a zero.")
    private BigDecimal montant;

    @NotBlank(message = "Le mode de paiement est obligatoire.")
    @Size(min = 2, max = 50, message = "Le mode de paiement doit contenir entre 2 et 50 caracteres.")
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ ]+$", message = "Le mode de paiement ne doit contenir que des lettres et espaces.")
    private String modePaiement;

    @Size(max = 120, message = "Le libelle ne doit pas depasser 120 caracteres.")
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ0-9'()\\- ,.]*$", message = "Le libelle contient des caracteres non autorises.")
    private String libelle;

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}
