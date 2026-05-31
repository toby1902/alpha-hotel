package com.alpha.hotel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SejourSchedulerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SejourSchedulerService.class);

    private final ReservationService reservationService;

    public SejourSchedulerService(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(cron = "${app.scheduler.auto-checkout-cron:0 10 0 * * *}")
    public void cloturerAutomatiquementLesSejoursEchus() {
        List<com.alpha.hotel.model.Reservation> reservationsACloturer = reservationService.recupererSejoursACloturerAujourdHui();
        int nombreClotures = reservationService.cloturerAutomatiquementSejoursEchus();
        if (nombreClotures > 0) {
            reservationService.notifierAdminClotureAutomatique(reservationsACloturer);
            LOGGER.info("Cloture automatique effectuee pour {} sejour(s) echu(s).", nombreClotures);
        } else {
            LOGGER.debug("Aucun sejour echu a cloturer automatiquement.");
        }
    }

    @Scheduled(cron = "${app.scheduler.depart-reminder-cron:0 0 18 * * *}")
    public void envoyerLesRappelsDeDepart() {
        int nombreRappels = reservationService.envoyerRappelsAvantDepart();
        if (nombreRappels > 0) {
            LOGGER.info("Rappel de depart envoye a {} client(s).", nombreRappels);
        } else {
            LOGGER.debug("Aucun rappel de depart a envoyer aujourd'hui.");
        }
    }

    @Scheduled(cron = "${app.scheduler.admin-depart-summary-cron:0 0 7 * * *}")
    public void envoyerLeRecapitulatifDesDepartsDuJour() {
        List<com.alpha.hotel.model.Reservation> departsDuJour = reservationService.recupererDepartsDuJour();
        if (!departsDuJour.isEmpty()) {
            reservationService.notifierAdminDepartsDuJour(departsDuJour);
            LOGGER.info("Recapitulatif des departs du jour envoye pour {} sejour(s).", departsDuJour.size());
        } else {
            LOGGER.debug("Aucun depart du jour a notifier aux administrateurs.");
        }
    }
}
