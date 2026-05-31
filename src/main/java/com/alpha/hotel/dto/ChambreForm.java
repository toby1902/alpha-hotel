package com.alpha.hotel.dto;

import com.alpha.hotel.model.enums.TypeChambre;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ChambreForm {

    @NotBlank(message = "Le numero de chambre est obligatoire.")
    @Pattern(regexp = "^[A-Za-z0-9-]{1,20}$", message = "Le numero de chambre ne doit contenir que des lettres, chiffres ou tirets.")
    private String numero;

    @NotNull(message = "Le type de chambre est obligatoire.")
    private TypeChambre type;

    @NotNull(message = "Le prix par nuit est obligatoire.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit etre superieur a zero.")
    private BigDecimal prixParNuit;

    @NotNull(message = "La capacite est obligatoire.")
    @Min(value = 1, message = "La capacite minimale est de 1.")
    private Integer capacite;

    @NotNull(message = "La disponibilite est obligatoire.")
    private Boolean disponible;

    @NotBlank(message = "La description est obligatoire.")
    @Size(min = 5, max = 255, message = "La description doit contenir entre 5 et 255 caracteres.")
    private String description;

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public TypeChambre getType() {
        return type;
    }

    public void setType(TypeChambre type) {
        this.type = type;
    }

    public BigDecimal getPrixParNuit() {
        return prixParNuit;
    }

    public void setPrixParNuit(BigDecimal prixParNuit) {
        this.prixParNuit = prixParNuit;
    }

    public Integer getCapacite() {
        return capacite;
    }

    public void setCapacite(Integer capacite) {
        this.capacite = capacite;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
