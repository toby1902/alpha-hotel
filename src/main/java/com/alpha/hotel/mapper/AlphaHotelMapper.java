package com.alpha.hotel.mapper;

import com.alpha.hotel.dto.ChambreResponse;
import com.alpha.hotel.dto.FactureResponse;
import com.alpha.hotel.dto.ReservationResponse;
import com.alpha.hotel.dto.StockResponse;
import com.alpha.hotel.model.Chambre;
import com.alpha.hotel.model.Facture;
import com.alpha.hotel.model.Reservation;
import com.alpha.hotel.model.StockPPN;
import org.springframework.stereotype.Component;

@Component
public class AlphaHotelMapper {

    public ChambreResponse toChambreResponse(Chambre chambre) {
        ChambreResponse response = new ChambreResponse();
        response.setId(chambre.getId());
        response.setNumero(chambre.getNumero());
        response.setType(chambre.getType().name());
        response.setPrixParNuit(chambre.getPrixParNuit());
        response.setCapacite(chambre.getCapacite());
        response.setDisponible(chambre.getDisponible());
        response.setDescription(chambre.getDescription());
        return response;
    }

    public ReservationResponse toReservationResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        response.setClientNom(reservation.getClient().getNomComplet());
        response.setClientEmail(reservation.getClient().getEmail());
        response.setChambreNumero(reservation.getChambre().getNumero());
        response.setTypeChambre(reservation.getChambre().getType().name());
        response.setDateArrivee(reservation.getDateArrivee());
        response.setDateDepart(reservation.getDateDepart());
        response.setMontantTotal(reservation.getMontantTotal());
        response.setAcompte(reservation.getAcompte());
        response.setStatut(reservation.getStatut().name());
        response.setStatutSejour(reservation.getStatutSejour().name());
        response.setDateCheckIn(reservation.getDateCheckIn());
        response.setDateCheckOut(reservation.getDateCheckOut());
        return response;
    }

    public StockResponse toStockResponse(StockPPN stock) {
        StockResponse response = new StockResponse();
        response.setId(stock.getId());
        response.setNomProduit(stock.getNomProduit());
        response.setQuantite(stock.getQuantite());
        response.setUnite(stock.getUnite());
        response.setPrixUnitaire(stock.getPrixUnitaire());
        response.setAlerteActive(stock.getAlerteActive());
        response.setMessageAlerte(stock.getMessageAlerte());
        return response;
    }

    public FactureResponse toFactureResponse(Facture facture) {
        FactureResponse response = new FactureResponse();
        response.setId(facture.getId());
        response.setNumeroFacture(facture.getNumeroFacture());
        response.setReservationId(facture.getReservation().getId());
        response.setDateEmission(facture.getDateEmission());
        response.setMontantTotal(facture.getMontantTotal());
        response.setMontantPaye(facture.getMontantPaye());
        response.setResteAPayer(facture.getResteAPayer());
        response.setStatut(facture.getStatut().name());
        return response;
    }
}
