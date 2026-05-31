package com.alpha.hotel.service;

import com.alpha.hotel.dto.ChambreForm;
import com.alpha.hotel.model.Chambre;
import com.alpha.hotel.model.enums.StatutReservation;
import com.alpha.hotel.repository.ChambreRepository;
import com.alpha.hotel.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class ChambreService {

    private final ChambreRepository chambreRepository;
    private final ReservationRepository reservationRepository;

    public ChambreService(ChambreRepository chambreRepository, ReservationRepository reservationRepository) {
        this.chambreRepository = chambreRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Chambre> getChambresDisponibles() {
        return chambreRepository.findByDisponibleTrueOrderByPrixParNuitAsc();
    }

    public List<Chambre> rechercherDisponibles(LocalDate dateArrivee, LocalDate dateDepart) {
        if (dateArrivee == null || dateDepart == null || !dateDepart.isAfter(dateArrivee)) {
            throw new IllegalArgumentException("Les dates de recherche sont invalides.");
        }

        Set<Long> chambresOccupees = Set.copyOf(
                reservationRepository.findChambreIdsOccupees(dateArrivee, dateDepart, StatutReservation.ANNULEE)
        );

        return getChambresDisponibles().stream()
                .filter(chambre -> !chambresOccupees.contains(chambre.getId()))
                .toList();
    }

    public boolean estDisponible(Long chambreId, LocalDate dateArrivee, LocalDate dateDepart) {
        return rechercherDisponibles(dateArrivee, dateDepart).stream()
                .anyMatch(chambre -> chambre.getId().equals(chambreId));
    }

    public boolean estDisponiblePourModification(Long chambreId, LocalDate dateArrivee, LocalDate dateDepart, Long reservationId) {
        if (dateArrivee == null || dateDepart == null || !dateDepart.isAfter(dateArrivee)) {
            throw new IllegalArgumentException("Les dates de recherche sont invalides.");
        }

        Set<Long> chambresOccupees = Set.copyOf(
                reservationRepository.findChambreIdsOccupeesEnExcluantReservation(dateArrivee, dateDepart, StatutReservation.ANNULEE, reservationId)
        );

        return getChambresDisponibles().stream()
                .filter(chambre -> chambre.getId().equals(chambreId))
                .anyMatch(chambre -> !chambresOccupees.contains(chambre.getId()));
    }

    public List<Chambre> listerToutes() {
        return chambreRepository.findAll().stream()
                .sorted((a, b) -> a.getNumero().compareToIgnoreCase(b.getNumero()))
                .toList();
    }

    public Chambre getById(Long id) {
        return chambreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chambre introuvable."));
    }

    @Transactional
    public Chambre creerChambre(ChambreForm form) {
        chambreRepository.findByNumero(form.getNumero())
                .ifPresent(chambre -> {
                    throw new IllegalArgumentException("Une chambre avec ce numero existe deja.");
                });

        Chambre chambre = new Chambre();
        appliquerForm(chambre, form);
        return chambreRepository.save(chambre);
    }

    @Transactional
    public Chambre modifierChambre(Long id, ChambreForm form) {
        Chambre chambre = getById(id);
        chambreRepository.findByNumero(form.getNumero())
                .filter(existante -> !existante.getId().equals(id))
                .ifPresent(existante -> {
                    throw new IllegalArgumentException("Une autre chambre utilise deja ce numero.");
                });

        appliquerForm(chambre, form);
        return chambreRepository.save(chambre);
    }

    @Transactional
    public Chambre basculerDisponibilite(Long id) {
        Chambre chambre = getById(id);
        chambre.setDisponible(!Boolean.TRUE.equals(chambre.getDisponible()));
        return chambreRepository.save(chambre);
    }

    private void appliquerForm(Chambre chambre, ChambreForm form) {
        chambre.setNumero(form.getNumero());
        chambre.setType(form.getType());
        chambre.setPrixParNuit(form.getPrixParNuit());
        chambre.setCapacite(form.getCapacite());
        chambre.setDisponible(form.getDisponible());
        chambre.setDescription(form.getDescription());
    }
}
