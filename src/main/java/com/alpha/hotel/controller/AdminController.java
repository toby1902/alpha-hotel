package com.alpha.hotel.controller;

import com.alpha.hotel.dto.ChambreForm;
import com.alpha.hotel.dto.AdminPaiementForm;
import com.alpha.hotel.dto.DisponibiliteSearchForm;
import com.alpha.hotel.dto.PaiementForm;
import com.alpha.hotel.dto.ReservationForm;
import com.alpha.hotel.dto.ReservationUpdateForm;
import com.alpha.hotel.dto.StockForm;
import com.alpha.hotel.model.enums.ModePaiement;
import com.alpha.hotel.model.enums.StatutReservation;
import com.alpha.hotel.model.enums.StatutSejour;
import com.alpha.hotel.model.enums.TypeChambre;
import com.alpha.hotel.service.ChambreService;
import com.alpha.hotel.service.FacturationService;
import com.alpha.hotel.service.ReservationService;
import com.alpha.hotel.service.StockService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ReservationService reservationService;
    private final StockService stockService;
    private final FacturationService facturationService;
    private final ChambreService chambreService;

    public AdminController(ReservationService reservationService,
                           StockService stockService,
                           FacturationService facturationService,
                           ChambreService chambreService) {
        this.reservationService = reservationService;
        this.stockService = stockService;
        this.facturationService = facturationService;
        this.chambreService = chambreService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String recherche,
                            @RequestParam(required = false) String statut,
                            @RequestParam(required = false) String statutSejour,
                            Model model) {
        model.addAttribute("reservations", reservationService.listerReservationsFiltrees(recherche, statut, statutSejour));
        model.addAttribute("stocks", stockService.listerStocks());
        model.addAttribute("alertes", stockService.recupererAlertesActives());
        model.addAttribute("stockForm", new StockForm());
        model.addAttribute("paiementForm", new PaiementForm());
        model.addAttribute("chambreForm", new ChambreForm());
        model.addAttribute("chambres", chambreService.listerToutes());
        model.addAttribute("arriveesDuJour", reservationService.recupererArriveesDuJour());
        model.addAttribute("departsDuJour", reservationService.recupererDepartsDuJour());
        var stats = reservationService.calculerStatistiques();
        stats.setStocksCritiques(stockService.recupererAlertesActives().size());
        model.addAttribute("stats", stats);
        model.addAttribute("typesChambre", TypeChambre.values());
        model.addAttribute("statutsReservation", StatutReservation.values());
        model.addAttribute("statutsSejour", StatutSejour.values());
        model.addAttribute("recherche", recherche == null ? "" : recherche);
        model.addAttribute("statutSelectionne", statut == null ? "" : statut);
        model.addAttribute("statutSejourSelectionne", statutSejour == null ? "" : statutSejour);
        return "admin-dashboard";
    }

    @GetMapping("/reservations/nouvelle")
    public String afficherFormulaireReservationAdmin(@RequestParam(required = false) LocalDate dateArrivee,
                                                     @RequestParam(required = false) LocalDate dateDepart,
                                                     @RequestParam(required = false) Long chambreId,
                                                     Model model) {
        ReservationForm reservationForm = new ReservationForm();
        reservationForm.setDateArrivee(dateArrivee);
        reservationForm.setDateDepart(dateDepart);
        reservationForm.setChambreId(chambreId);
        ajouterAttributsFormulaireReservationAdmin(model, reservationForm, dateArrivee, dateDepart);
        return "admin-reservation-form";
    }

    @PostMapping("/reservations/nouvelle")
    public String creerReservationAdmin(@Valid @ModelAttribute("reservationForm") ReservationForm reservationForm,
                                        BindingResult bindingResult,
                                        Model model) {
        if (bindingResult.hasErrors()) {
            ajouterAttributsFormulaireReservationAdmin(model, reservationForm, reservationForm.getDateArrivee(), reservationForm.getDateDepart());
            return "admin-reservation-form";
        }

        try {
            reservationService.creerReservation(reservationForm);
            return "redirect:/admin/dashboard?reservationCreee";
        } catch (IllegalArgumentException exception) {
            ajouterAttributsFormulaireReservationAdmin(model, reservationForm, reservationForm.getDateArrivee(), reservationForm.getDateDepart());
            model.addAttribute("erreurReservation", exception.getMessage());
            return "admin-reservation-form";
        }
    }

    @GetMapping("/paiements/nouveau")
    public String afficherFormulairePaiementAdmin(Model model) {
        ajouterAttributsFormulairePaiementAdmin(model, new AdminPaiementForm());
        return "admin-payment-form";
    }

    @PostMapping("/paiements/nouveau")
    public String enregistrerPaiementDepuisFormulaire(@Valid @ModelAttribute("adminPaiementForm") AdminPaiementForm adminPaiementForm,
                                                      BindingResult bindingResult,
                                                      Model model) {
        if (bindingResult.hasErrors()) {
            ajouterAttributsFormulairePaiementAdmin(model, adminPaiementForm);
            return "admin-payment-form";
        }
        PaiementForm paiementForm = new PaiementForm();
        paiementForm.setMontant(adminPaiementForm.getMontant());
        paiementForm.setModePaiement(adminPaiementForm.getModePaiement());
        paiementForm.setLibelle(adminPaiementForm.getLibelle());
        facturationService.enregistrerPaiement(adminPaiementForm.getReservationId(), paiementForm);
        return "redirect:/admin/dashboard?paiementOk";
    }

    @GetMapping("/stocks/nouveau")
    public String afficherFormulaireStockAdmin(Model model) {
        ajouterAttributsFormulaireStockAdmin(model, new StockForm());
        return "admin-stock-form";
    }

    @GetMapping("/chambres/nouvelle")
    public String afficherFormulaireChambreAdmin(Model model) {
        ajouterAttributsFormulaireChambreAdmin(model, new ChambreForm());
        return "admin-room-form";
    }

    @PostMapping("/reservations/{id}/valider")
    public String validerReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.validerReservation(id);
            return "redirect:/admin/dashboard?reservationValidee";
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("erreurEmailConfirmation", exception.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/reservations/{id}/check-in")
    public String checkIn(@PathVariable Long id) {
        reservationService.effectuerCheckIn(id);
        return "redirect:/admin/dashboard?checkinOk";
    }

    @PostMapping("/reservations/{id}/check-out")
    public String checkOut(@PathVariable Long id) {
        reservationService.effectuerCheckOut(id);
        return "redirect:/admin/dashboard?checkoutOk";
    }

    @PostMapping("/reservations/{id}/annuler")
    public String annulerReservation(@PathVariable Long id) {
        reservationService.annulerReservation(id);
        return "redirect:/admin/dashboard?annulationOk";
    }

    @GetMapping("/reservations/{id}/modifier")
    public String afficherFormulaireModification(@PathVariable Long id,
                                                 @RequestParam(required = false) String recherche,
                                                 @RequestParam(required = false) String statut,
                                                 @RequestParam(required = false) String statutSejour,
                                                 Model model) {
        dashboard(recherche, statut, statutSejour, model);
        model.addAttribute("reservationEditId", id);
        model.addAttribute("reservationUpdateForm", reservationService.buildUpdateForm(id));
        return "admin-dashboard";
    }

    @PostMapping("/reservations/{id}/modifier")
    public String modifierReservation(@PathVariable Long id,
                                      @Valid @ModelAttribute("reservationUpdateForm") ReservationUpdateForm form,
                                      BindingResult bindingResult,
                                      Model model) {
        if (bindingResult.hasErrors()) {
            dashboard(null, null, null, model);
            model.addAttribute("reservationEditId", id);
            return "admin-dashboard";
        }
        reservationService.modifierReservation(id, form);
        return "redirect:/admin/dashboard?reservationModifiee";
    }

    @PostMapping("/reservations/{id}/paiements")
    public String enregistrerPaiement(@PathVariable Long id,
                                      @Valid @ModelAttribute("paiementForm") PaiementForm paiementForm,
                                      BindingResult bindingResult,
                                      Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("reservations", reservationService.listerReservations());
            model.addAttribute("stocks", stockService.listerStocks());
            model.addAttribute("alertes", stockService.recupererAlertesActives());
            model.addAttribute("stockForm", new StockForm());
            return "admin-dashboard";
        }
        facturationService.enregistrerPaiement(id, paiementForm);
        return "redirect:/admin/dashboard?paiementOk";
    }

    @PostMapping("/stocks")
    public String ajouterStock(@Valid @ModelAttribute("stockForm") StockForm stockForm,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            ajouterAttributsFormulaireStockAdmin(model, stockForm);
            return "admin-stock-form";
        }

        stockService.enregistrer(stockForm);
        return "redirect:/admin/dashboard?stockSaved";
    }

    @GetMapping("/reservations/{id}/facture/pdf")
    public ResponseEntity<byte[]> telechargerFacturePdf(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            byte[] pdf = facturationService.genererFacturePdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline().filename("facture-reservation-" + id + ".pdf").build());
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("erreurFacturePdf", exception.getMessage());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers)
                    .body(exception.getMessage().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/reservations/export/csv")
    public ResponseEntity<byte[]> exporterReservations(@RequestParam(required = false) String recherche,
                                                       @RequestParam(required = false) String statut,
                                                       @RequestParam(required = false) String statutSejour) throws IOException {
        byte[] csv = reservationService.exporterReservationsCsv(recherche, statut, statutSejour).readAllBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv"));
        headers.setContentDisposition(ContentDisposition.attachment().filename("reservations-alpha-hotel.csv").build());
        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }

    @PostMapping("/chambres")
    public String ajouterChambre(@Valid @ModelAttribute("chambreForm") ChambreForm chambreForm,
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            ajouterAttributsFormulaireChambreAdmin(model, chambreForm);
            return "admin-room-form";
        }
        chambreService.creerChambre(chambreForm);
        return "redirect:/admin/dashboard?chambreAjoutee";
    }

    @PostMapping("/chambres/{id}/disponibilite")
    public String basculerDisponibiliteChambre(@PathVariable Long id) {
        chambreService.basculerDisponibilite(id);
        return "redirect:/admin/dashboard?chambreModifiee";
    }

    @GetMapping("/chambres/disponibles")
    public String rechercherChambresDisponiblesAdmin(@Valid @ModelAttribute("searchForm") DisponibiliteSearchForm searchForm,
                                                     BindingResult bindingResult,
                                                     Model model) {
        model.addAttribute("searchDone", searchForm.getDateArrivee() != null || searchForm.getDateDepart() != null);
        model.addAttribute("today", LocalDate.now());

        if (searchForm.getDateArrivee() == null && searchForm.getDateDepart() == null) {
            model.addAttribute("searchForm", reservationService.buildDisponibiliteSearchForm());
            model.addAttribute("chambresDisponibles", java.util.List.of());
            return "admin-availability";
        }

        if (!bindingResult.hasErrors() && !searchForm.getDateDepart().isAfter(searchForm.getDateArrivee())) {
            bindingResult.rejectValue("dateDepart", "dateDepart.invalid", "La date de depart doit etre posterieure a la date d'arrivee.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("chambresDisponibles", java.util.List.of());
            return "admin-availability";
        }

        model.addAttribute("chambresDisponibles", chambreService.rechercherDisponibles(searchForm.getDateArrivee(), searchForm.getDateDepart()));
        model.addAttribute("nombreNuits", java.time.temporal.ChronoUnit.DAYS.between(searchForm.getDateArrivee(), searchForm.getDateDepart()));
        return "admin-availability";
    }

    private void ajouterAttributsFormulaireReservationAdmin(Model model,
                                                            ReservationForm reservationForm,
                                                            LocalDate dateArrivee,
                                                            LocalDate dateDepart) {
        model.addAttribute("reservationForm", reservationForm);
        model.addAttribute("modesPaiement", ModePaiement.values());
        if (dateArrivee != null && dateDepart != null && dateDepart.isAfter(dateArrivee)) {
            model.addAttribute("chambres", chambreService.rechercherDisponibles(dateArrivee, dateDepart));
        } else {
            model.addAttribute("chambres", chambreService.getChambresDisponibles());
        }
    }

    private void ajouterAttributsFormulairePaiementAdmin(Model model, AdminPaiementForm adminPaiementForm) {
        model.addAttribute("adminPaiementForm", adminPaiementForm);
        model.addAttribute("reservations", reservationService.listerReservations());
    }

    private void ajouterAttributsFormulaireStockAdmin(Model model, StockForm stockForm) {
        model.addAttribute("stockForm", stockForm);
        model.addAttribute("stocks", stockService.listerStocks());
        model.addAttribute("alertes", stockService.recupererAlertesActives());
    }

    private void ajouterAttributsFormulaireChambreAdmin(Model model, ChambreForm chambreForm) {
        model.addAttribute("chambreForm", chambreForm);
        model.addAttribute("typesChambre", TypeChambre.values());
    }
}
