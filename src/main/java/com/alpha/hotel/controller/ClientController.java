package com.alpha.hotel.controller;

import com.alpha.hotel.dto.DisponibiliteSearchForm;
import com.alpha.hotel.dto.ReservationForm;
import com.alpha.hotel.model.enums.ModePaiement;
import com.alpha.hotel.service.ChambreService;
import com.alpha.hotel.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping("/reservations")
public class ClientController {

    private final ChambreService chambreService;
    private final ReservationService reservationService;

    public ClientController(ChambreService chambreService, ReservationService reservationService) {
        this.chambreService = chambreService;
        this.reservationService = reservationService;
    }

    @GetMapping("/nouvelle")
    public String showReservationForm(@RequestParam(required = false) LocalDate dateArrivee,
                                      @RequestParam(required = false) LocalDate dateDepart,
                                      @RequestParam(required = false) Long chambreId,
                                      Model model) {
        ReservationForm reservationForm = new ReservationForm();
        reservationForm.setDateArrivee(dateArrivee);
        reservationForm.setDateDepart(dateDepart);
        reservationForm.setChambreId(chambreId);

        if (dateArrivee != null && dateDepart != null && dateDepart.isAfter(dateArrivee)) {
            model.addAttribute("chambres", chambreService.rechercherDisponibles(dateArrivee, dateDepart));
        } else {
            model.addAttribute("chambres", chambreService.getChambresDisponibles());
        }
        model.addAttribute("reservationForm", reservationForm);
        model.addAttribute("modesPaiement", ModePaiement.values());
        return "reservation-form";
    }

    @PostMapping("/nouvelle")
    public String submitReservation(@Valid @ModelAttribute("reservationForm") ReservationForm reservationForm,
                                    BindingResult bindingResult,
                                    Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("chambres", chambreService.getChambresDisponibles());
            model.addAttribute("modesPaiement", ModePaiement.values());
            return "reservation-form";
        }

        try {
            reservationService.creerReservation(reservationForm);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("chambres", chambreService.getChambresDisponibles());
            model.addAttribute("modesPaiement", ModePaiement.values());
            model.addAttribute("erreurReservation", exception.getMessage());
            return "reservation-form";
        }
        return "redirect:/reservations/nouvelle?success";
    }

    @GetMapping("/disponibilites")
    public String rechercherDisponibilitesClient(@Valid @ModelAttribute("searchForm") DisponibiliteSearchForm searchForm,
                                                 BindingResult bindingResult,
                                                 Model model) {
        model.addAttribute("searchDone", searchForm.getDateArrivee() != null || searchForm.getDateDepart() != null);

        if (searchForm.getDateArrivee() == null && searchForm.getDateDepart() == null) {
            model.addAttribute("searchForm", reservationService.buildDisponibiliteSearchForm());
            model.addAttribute("chambresDisponibles", java.util.List.of());
            return "availability-search";
        }

        if (!bindingResult.hasErrors() && !searchForm.getDateDepart().isAfter(searchForm.getDateArrivee())) {
            bindingResult.rejectValue("dateDepart", "dateDepart.invalid", "La date de depart doit etre posterieure a la date d'arrivee.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("chambresDisponibles", java.util.List.of());
            return "availability-search";
        }

        model.addAttribute("chambresDisponibles", chambreService.rechercherDisponibles(searchForm.getDateArrivee(), searchForm.getDateDepart()));
        model.addAttribute("nombreNuits", java.time.temporal.ChronoUnit.DAYS.between(searchForm.getDateArrivee(), searchForm.getDateDepart()));
        return "availability-search";
    }
}
