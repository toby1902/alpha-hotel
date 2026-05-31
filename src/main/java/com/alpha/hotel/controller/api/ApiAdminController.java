package com.alpha.hotel.controller.api;

import com.alpha.hotel.dto.ChambreForm;
import com.alpha.hotel.dto.ChambreResponse;
import com.alpha.hotel.dto.ApiMessageResponse;
import com.alpha.hotel.dto.FactureResponse;
import com.alpha.hotel.dto.PaiementForm;
import com.alpha.hotel.dto.ReservationResponse;
import com.alpha.hotel.dto.ReservationUpdateForm;
import com.alpha.hotel.dto.StockForm;
import com.alpha.hotel.dto.StockResponse;
import com.alpha.hotel.mapper.AlphaHotelMapper;
import com.alpha.hotel.model.Facture;
import com.alpha.hotel.model.Paiement;
import com.alpha.hotel.model.Reservation;
import com.alpha.hotel.model.StockPPN;
import com.alpha.hotel.model.Chambre;
import com.alpha.hotel.service.ChambreService;
import com.alpha.hotel.service.FacturationService;
import com.alpha.hotel.service.ReservationService;
import com.alpha.hotel.service.StockService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class ApiAdminController {

    private final ReservationService reservationService;
    private final StockService stockService;
    private final FacturationService facturationService;
    private final ChambreService chambreService;
    private final AlphaHotelMapper mapper;

    public ApiAdminController(ReservationService reservationService,
                              StockService stockService,
                              FacturationService facturationService,
                              ChambreService chambreService,
                              AlphaHotelMapper mapper) {
        this.reservationService = reservationService;
        this.stockService = stockService;
        this.facturationService = facturationService;
        this.chambreService = chambreService;
        this.mapper = mapper;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> listerReservations() {
        return ResponseEntity.ok(
                reservationService.listerReservations().stream()
                        .map(mapper::toReservationResponse)
                        .toList()
        );
    }

    @PostMapping("/reservations/{id}/valider")
    public ResponseEntity<ReservationResponse> validerReservation(@PathVariable Long id) {
        Reservation reservation = reservationService.validerReservation(id);
        return ResponseEntity.ok(mapper.toReservationResponse(reservation));
    }

    @PostMapping("/reservations/{id}/annuler")
    public ResponseEntity<ReservationResponse> annulerReservation(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toReservationResponse(reservationService.annulerReservation(id)));
    }

    @PostMapping("/reservations/{id}/modifier")
    public ResponseEntity<ReservationResponse> modifierReservation(@PathVariable Long id,
                                                                  @Valid @RequestBody ReservationUpdateForm form) {
        return ResponseEntity.ok(mapper.toReservationResponse(reservationService.modifierReservation(id, form)));
    }

    @PostMapping("/reservations/{id}/check-in")
    public ResponseEntity<ReservationResponse> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toReservationResponse(reservationService.effectuerCheckIn(id)));
    }

    @PostMapping("/reservations/{id}/check-out")
    public ResponseEntity<ReservationResponse> checkOut(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toReservationResponse(reservationService.effectuerCheckOut(id)));
    }

    @PostMapping("/reservations/{id}/paiements")
    public ResponseEntity<ApiMessageResponse> enregistrerPaiement(@PathVariable Long id,
                                                                  @Valid @RequestBody PaiementForm paiementForm) {
        Paiement paiement = facturationService.enregistrerPaiement(id, paiementForm);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiMessageResponse("Paiement enregistre : " + paiement.getMontant() + " Ar."));
    }

    @GetMapping("/reservations/{id}/facture")
    public ResponseEntity<FactureResponse> recupererFacture(@PathVariable Long id) {
        Facture facture = facturationService.recupererFactureParReservation(id);
        return ResponseEntity.ok(mapper.toFactureResponse(facture));
    }

    @GetMapping("/reservations/{id}/facture/pdf")
    public ResponseEntity<byte[]> telechargerFacturePdf(@PathVariable Long id) {
        byte[] pdf = facturationService.genererFacturePdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=facture-reservation-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/stocks")
    public ResponseEntity<List<StockResponse>> listerStocks() {
        return ResponseEntity.ok(
                stockService.listerStocks().stream()
                        .map(mapper::toStockResponse)
                        .toList()
        );
    }

    @GetMapping("/stocks/alertes")
    public ResponseEntity<List<StockResponse>> listerAlertes() {
        return ResponseEntity.ok(
                stockService.recupererAlertesActives().stream()
                        .map(mapper::toStockResponse)
                        .toList()
        );
    }

    @PostMapping("/stocks")
    public ResponseEntity<?> creerStock(@Valid @RequestBody StockForm stockForm) {
        StockPPN stock = stockService.enregistrer(stockForm);
        if (Boolean.TRUE.equals(stock.getAlerteActive())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toStockResponse(stock));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiMessageResponse("Produit PPN enregistre avec succes."));
    }

    @GetMapping("/chambres")
    public ResponseEntity<List<ChambreResponse>> listerChambres() {
        return ResponseEntity.ok(chambreService.listerToutes().stream()
                .map(mapper::toChambreResponse)
                .toList());
    }

    @PostMapping("/chambres")
    public ResponseEntity<ChambreResponse> creerChambre(@Valid @RequestBody ChambreForm chambreForm) {
        Chambre chambre = chambreService.creerChambre(chambreForm);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toChambreResponse(chambre));
    }

    @PostMapping("/chambres/{id}/disponibilite")
    public ResponseEntity<ChambreResponse> basculerDisponibiliteChambre(@PathVariable Long id) {
        Chambre chambre = chambreService.basculerDisponibilite(id);
        return ResponseEntity.ok(mapper.toChambreResponse(chambre));
    }
}
