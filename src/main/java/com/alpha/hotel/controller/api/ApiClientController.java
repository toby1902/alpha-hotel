package com.alpha.hotel.controller.api;

import com.alpha.hotel.dto.ChambreResponse;
import com.alpha.hotel.dto.ReservationForm;
import com.alpha.hotel.dto.ReservationResponse;
import com.alpha.hotel.model.enums.ModePaiement;
import com.alpha.hotel.mapper.AlphaHotelMapper;
import com.alpha.hotel.model.Reservation;
import com.alpha.hotel.service.ChambreService;
import com.alpha.hotel.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiClientController {

    private final ChambreService chambreService;
    private final ReservationService reservationService;
    private final AlphaHotelMapper mapper;

    public ApiClientController(ChambreService chambreService,
                               ReservationService reservationService,
                               AlphaHotelMapper mapper) {
        this.chambreService = chambreService;
        this.reservationService = reservationService;
        this.mapper = mapper;
    }

    @GetMapping("/chambres")
    public ResponseEntity<List<ChambreResponse>> listerChambresDisponibles() {
        return ResponseEntity.ok(
                chambreService.getChambresDisponibles().stream()
                        .map(mapper::toChambreResponse)
                        .toList()
        );
    }

    @GetMapping("/modes-paiement")
    public ResponseEntity<List<Map<String, String>>> listerModesPaiement() {
        return ResponseEntity.ok(
                java.util.Arrays.stream(ModePaiement.values())
                        .map(mode -> Map.of("code", mode.name(), "label", mode.name().replace('_', ' ')))
                        .toList()
        );
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> creerReservation(@Valid @RequestBody ReservationForm form) {
        Reservation reservation = reservationService.creerReservation(form);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toReservationResponse(reservation));
    }
}
