package com.alpha.hotel.service;

import com.alpha.hotel.dto.DashboardStats;
import com.alpha.hotel.dto.DisponibiliteSearchForm;
import com.alpha.hotel.dto.ReservationForm;
import com.alpha.hotel.dto.ReservationUpdateForm;
import com.alpha.hotel.model.Chambre;
import com.alpha.hotel.model.Client;
import com.alpha.hotel.model.Paiement;
import com.alpha.hotel.model.Reservation;
import com.alpha.hotel.model.enums.StatutReservation;
import com.alpha.hotel.model.enums.StatutSejour;
import com.alpha.hotel.model.enums.StatutPaiement;
import com.alpha.hotel.model.enums.RoleUtilisateur;
import com.alpha.hotel.repository.PaiementRepository;
import com.alpha.hotel.repository.ClientRepository;
import com.alpha.hotel.repository.ReservationRepository;
import com.alpha.hotel.repository.UtilisateurRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
public class ReservationService {

    private static final String ADRESSE_HOTEL = "Atsimondrova Ambatondrazaka 503";
    private static final String CONTACT_HOTEL = "0349733147 / 0381219163";

    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final ChambreService chambreService;
    private final EmailService emailService;
    private final PaiementRepository paiementRepository;
    private final FacturationService facturationService;
    private final UtilisateurRepository utilisateurRepository;

    @Value("${app.mail.notification-admin}")
    private String notificationAdmin;

    public ReservationService(ReservationRepository reservationRepository,
                              ClientRepository clientRepository,
                              ChambreService chambreService,
                              EmailService emailService,
                              PaiementRepository paiementRepository,
                              FacturationService facturationService,
                              UtilisateurRepository utilisateurRepository) {
        this.reservationRepository = reservationRepository;
        this.clientRepository = clientRepository;
        this.chambreService = chambreService;
        this.emailService = emailService;
        this.paiementRepository = paiementRepository;
        this.facturationService = facturationService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional
    public Reservation creerReservation(ReservationForm form) {
        // Validation metier supplementaire pour eviter un sejour incoherent.
        if (!form.getDateDepart().isAfter(form.getDateArrivee())) {
            throw new IllegalArgumentException("La date de depart doit etre posterieure a la date d'arrivee.");
        }
        if (!chambreService.estDisponible(form.getChambreId(), form.getDateArrivee(), form.getDateDepart())) {
            throw new IllegalArgumentException("La chambre choisie n'est plus disponible sur cette periode.");
        }

        Chambre chambre = chambreService.getById(form.getChambreId());
        Client client = clientRepository.findByEmail(form.getEmail())
                .orElseGet(() -> creerClient(form));

        client.setNomComplet(form.getNomComplet());
        client.setTelephone(form.getTelephone());
        client = clientRepository.save(client);

        Reservation reservation = new Reservation();
        reservation.setClient(client);
        reservation.setChambre(chambre);
        reservation.setDateArrivee(form.getDateArrivee());
        reservation.setDateDepart(form.getDateDepart());
        Reservation savedReservation = reservationRepository.save(reservation);
        enregistrerAvanceClient(savedReservation, form);
        facturationService.creerOuMettreAJourFacture(savedReservation);
        notifierAdministrateursNouvelleReservation(savedReservation);
        return savedReservation;
    }

    public List<Reservation> listerReservations() {
        return reservationRepository.findAll().stream()
                .sorted((a, b) -> {
                    int byArrival = b.getDateArrivee().compareTo(a.getDateArrivee());
                    return byArrival != 0 ? byArrival : b.getId().compareTo(a.getId());
                })
                .toList();
    }

    public List<Reservation> listerReservationsFiltrees(String recherche, String statut, String statutSejour) {
        return listerReservations().stream()
                .filter(reservation -> correspondRecherche(reservation, recherche))
                .filter(reservation -> statut == null || statut.isBlank() || reservation.getStatut().name().equalsIgnoreCase(statut))
                .filter(reservation -> statutSejour == null || statutSejour.isBlank() || reservation.getStatutSejour().name().equalsIgnoreCase(statutSejour))
                .toList();
    }

    @Transactional
    public Reservation validerReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation introuvable."));

        // Le changement de statut et l'email de confirmation sont traites dans la meme operation metier.
        reservation.setStatut(StatutReservation.CONFIRMEE);
        Reservation savedReservation = reservationRepository.save(reservation);
        facturationService.creerOuMettreAJourFacture(savedReservation);

        long nuits = ChronoUnit.DAYS.between(savedReservation.getDateArrivee(), savedReservation.getDateDepart());
        String emailHtml = """
                <html>
                <body style="font-family: Arial, sans-serif; color: #1f2937;">
                    <h2>Confirmation de reservation - Alpha Hotel</h2>
                    <p>Bonjour %s,</p>
                    <p>Votre reservation a ete confirmee avec succes.</p>
                    <ul>
                        <li>Date d'arrivee : %s</li>
                        <li>Date de depart : %s</li>
                        <li>Nombre de nuits : %d</li>
                        <li>Type de chambre : %s</li>
                        <li>Acompte paye : %s Ar</li>
                    </ul>
                    <p><strong>Adresse :</strong> %s</p>
                    <p><strong>Contact :</strong> %s</p>
                    <p>Nous vous remercions pour votre confiance.</p>
                </body>
                </html>
                """.formatted(
                savedReservation.getClient().getNomComplet(),
                savedReservation.getDateArrivee(),
                savedReservation.getDateDepart(),
                nuits,
                savedReservation.getChambre().getType(),
                savedReservation.getAcompte(),
                ADRESSE_HOTEL,
                CONTACT_HOTEL
        );

        boolean emailEnvoye = emailService.envoyerEmailHtml(
                savedReservation.getClient().getEmail(),
                "Confirmation de votre sejour Alpha Hotel",
                emailHtml
        );

        if (!emailEnvoye) {
            throw new IllegalStateException("Reservation confirmee, mais l'email n'a pas pu etre envoye. Verifiez la configuration SMTP.");
        }

        return savedReservation;
    }

    @Transactional
    public Reservation effectuerCheckIn(Long id) {
        Reservation reservation = getReservationParId(id);
        if (reservation.getStatut() != StatutReservation.CONFIRMEE) {
            throw new IllegalArgumentException("Le check-in est autorise uniquement pour une reservation confirmee.");
        }
        reservation.setStatutSejour(StatutSejour.EN_COURS);
        reservation.setDateCheckIn(LocalDate.now());
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation effectuerCheckOut(Long id) {
        Reservation reservation = getReservationParId(id);
        if (reservation.getStatutSejour() != StatutSejour.EN_COURS) {
            throw new IllegalArgumentException("Le check-out est autorise uniquement pour un sejour en cours.");
        }
        reservation.setStatutSejour(StatutSejour.TERMINE);
        reservation.setDateCheckOut(LocalDate.now());
        Reservation savedReservation = reservationRepository.save(reservation);
        facturationService.creerOuMettreAJourFacture(savedReservation);
        return savedReservation;
    }

    public Reservation getReservationParId(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation introuvable."));
    }

    @Transactional
    public Reservation annulerReservation(Long id) {
        Reservation reservation = getReservationParId(id);
        reservation.setStatut(StatutReservation.ANNULEE);
        reservation.setStatutSejour(StatutSejour.NON_COMMENCE);
        reservation.setDateCheckIn(null);
        reservation.setDateCheckOut(null);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation modifierReservation(Long id, ReservationUpdateForm form) {
        if (!form.getDateDepart().isAfter(form.getDateArrivee())) {
            throw new IllegalArgumentException("La date de depart doit etre posterieure a la date d'arrivee.");
        }
        if (!chambreService.estDisponiblePourModification(form.getChambreId(), form.getDateArrivee(), form.getDateDepart(), id)) {
            throw new IllegalArgumentException("La chambre choisie n'est pas disponible sur cette nouvelle periode.");
        }

        Reservation reservation = getReservationParId(id);
        Chambre chambre = chambreService.getById(form.getChambreId());
        Client client = reservation.getClient();

        client.setNomComplet(form.getNomComplet());
        client.setEmail(form.getEmail());
        client.setTelephone(form.getTelephone());

        reservation.setClient(clientRepository.save(client));
        reservation.setChambre(chambre);
        reservation.setDateArrivee(form.getDateArrivee());
        reservation.setDateDepart(form.getDateDepart());
        recalculerMontants(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);
        facturationService.creerOuMettreAJourFacture(savedReservation);
        return savedReservation;
    }

    public ReservationUpdateForm buildUpdateForm(Long id) {
        Reservation reservation = getReservationParId(id);
        ReservationUpdateForm form = new ReservationUpdateForm();
        form.setNomComplet(reservation.getClient().getNomComplet());
        form.setEmail(reservation.getClient().getEmail());
        form.setTelephone(reservation.getClient().getTelephone());
        form.setChambreId(reservation.getChambre().getId());
        form.setDateArrivee(reservation.getDateArrivee());
        form.setDateDepart(reservation.getDateDepart());
        return form;
    }

    public DashboardStats calculerStatistiques() {
        List<Reservation> reservations = reservationRepository.findAll();
        DashboardStats stats = new DashboardStats();
        stats.setTotalReservations(reservations.size());
        stats.setReservationsEnAttente(reservations.stream().filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE).count());
        stats.setReservationsConfirmees(reservations.stream().filter(r -> r.getStatut() == StatutReservation.CONFIRMEE).count());
        stats.setReservationsAnnulees(reservations.stream().filter(r -> r.getStatut() == StatutReservation.ANNULEE).count());
        stats.setMontantEncaisse(paiementRepository.findAll().stream()
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP));
        return stats;
    }

    @Transactional
    public int cloturerAutomatiquementSejoursEchus() {
        List<Reservation> sejoursAEteCloturer = reservationRepository
                .findByStatutSejourAndDateDepartBeforeAndStatut(
                        StatutSejour.EN_COURS,
                        LocalDate.now(),
                        StatutReservation.CONFIRMEE
                );

        for (Reservation reservation : sejoursAEteCloturer) {
            reservation.setStatutSejour(StatutSejour.TERMINE);
            if (reservation.getDateCheckOut() == null) {
                reservation.setDateCheckOut(reservation.getDateDepart());
            }
            facturationService.creerOuMettreAJourFacture(reservation);
        }

        reservationRepository.saveAll(sejoursAEteCloturer);
        return sejoursAEteCloturer.size();
    }

    @Transactional
    public int envoyerRappelsAvantDepart() {
        LocalDate dateCible = LocalDate.now().plusDays(1);
        List<Reservation> reservations = reservationRepository.findByDateDepartAndStatut(dateCible, StatutReservation.CONFIRMEE)
                .stream()
                .filter(reservation -> reservation.getStatutSejour() != StatutSejour.TERMINE)
                .filter(reservation -> !Boolean.TRUE.equals(reservation.getRappelDepartEnvoye()))
                .toList();

        int nombreEnvoyes = 0;
        for (Reservation reservation : reservations) {
            String emailHtml = """
                    <html>
                    <body style="font-family: Arial, sans-serif; color: #1f2937;">
                        <h2>Rappel de depart - Alpha Hotel</h2>
                        <p>Bonjour %s,</p>
                        <p>Nous vous rappelons que votre date de depart est prevue pour demain, le <strong>%s</strong>.</p>
                        <ul>
                            <li>Chambre : %s - %s</li>
                            <li>Date d'arrivee : %s</li>
                            <li>Date de depart : %s</li>
                        </ul>
                        <p>Si vous souhaitez preparer votre sortie ou obtenir une assistance, notre reception reste a votre disposition.</p>
                        <p><strong>Adresse :</strong> %s</p>
                        <p><strong>Contact :</strong> %s</p>
                        <p>Merci pour votre confiance et excellent sejour chez Alpha Hotel.</p>
                    </body>
                    </html>
                    """.formatted(
                    reservation.getClient().getNomComplet(),
                    reservation.getDateDepart(),
                    reservation.getChambre().getNumero(),
                    reservation.getChambre().getType(),
                    reservation.getDateArrivee(),
                    reservation.getDateDepart(),
                    ADRESSE_HOTEL,
                    CONTACT_HOTEL
            );

            boolean envoye = emailService.envoyerEmailHtml(
                    reservation.getClient().getEmail(),
                    "Rappel de votre depart demain - Alpha Hotel",
                    emailHtml
            );

            if (envoye) {
                reservation.setRappelDepartEnvoye(true);
                nombreEnvoyes++;
            }
        }

        reservationRepository.saveAll(reservations);
        return nombreEnvoyes;
    }

    public void notifierAdminClotureAutomatique(List<Reservation> reservationsCloturees) {
        if (reservationsCloturees == null || reservationsCloturees.isEmpty()) {
            return;
        }

        StringBuilder lignes = new StringBuilder();
        for (Reservation reservation : reservationsCloturees) {
            lignes.append("<li>")
                    .append("Reservation #").append(reservation.getId())
                    .append(" - ").append(reservation.getClient().getNomComplet())
                    .append(" - chambre ").append(reservation.getChambre().getNumero())
                    .append(" - depart prevu le ").append(reservation.getDateDepart())
                    .append("</li>");
        }

        String emailHtml = """
                <html>
                <body style="font-family: Arial, sans-serif; color: #1f2937;">
                    <h2>Cloture automatique de sejour - Alpha Hotel</h2>
                    <p>Le systeme a detecte et cloture automatiquement les sejours suivants, car leur date de depart est depassee :</p>
                    <ul>%s</ul>
                    <p>Merci de vous connecter au dashboard pour verification si necessaire.</p>
                    <p><strong>Lien :</strong> <a href="http://localhost:8080/auth/login">http://localhost:8080/auth/login</a></p>
                </body>
                </html>
                """.formatted(lignes);

        for (String destinataire : recupererDestinatairesAdministrateurs()) {
            emailService.envoyerEmailHtml(
                    destinataire,
                    "Sejours clotures automatiquement - Alpha Hotel",
                    emailHtml
            );
        }
    }

    public List<Reservation> recupererSejoursACloturerAujourdHui() {
        return reservationRepository.findByStatutSejourAndDateDepartBeforeAndStatut(
                StatutSejour.EN_COURS,
                LocalDate.now(),
                StatutReservation.CONFIRMEE
        );
    }

    public List<Reservation> recupererDepartsDuJour() {
        return reservationRepository.findByDateDepartAndStatut(LocalDate.now(), StatutReservation.CONFIRMEE)
                .stream()
                .filter(reservation -> reservation.getStatutSejour() != StatutSejour.TERMINE)
                .toList();
    }

    public List<Reservation> recupererArriveesDuJour() {
        return reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getStatut() == StatutReservation.CONFIRMEE)
                .filter(reservation -> LocalDate.now().equals(reservation.getDateArrivee()))
                .filter(reservation -> reservation.getStatutSejour() == StatutSejour.NON_COMMENCE)
                .sorted((a, b) -> a.getChambre().getNumero().compareToIgnoreCase(b.getChambre().getNumero()))
                .toList();
    }

    public void notifierAdminDepartsDuJour(List<Reservation> departsDuJour) {
        if (departsDuJour == null || departsDuJour.isEmpty()) {
            return;
        }

        StringBuilder lignes = new StringBuilder();
        for (Reservation reservation : departsDuJour) {
            lignes.append("<li>")
                    .append("Reservation #").append(reservation.getId())
                    .append(" - ").append(reservation.getClient().getNomComplet())
                    .append(" - chambre ").append(reservation.getChambre().getNumero())
                    .append(" - telephone ").append(reservation.getClient().getTelephone())
                    .append(" - statut sejour ").append(reservation.getStatutSejour())
                    .append("</li>");
        }

        String emailHtml = """
                <html>
                <body style="font-family: Arial, sans-serif; color: #1f2937;">
                    <h2>Departs prevus aujourd'hui - Alpha Hotel</h2>
                    <p>Voici la liste des clients dont le depart est prevu aujourd'hui :</p>
                    <ul>%s</ul>
                    <p>Merci de suivre les check-out depuis le dashboard administratif.</p>
                    <p><strong>Lien :</strong> <a href="http://localhost:8080/auth/login">http://localhost:8080/auth/login</a></p>
                    <p><strong>Adresse :</strong> %s</p>
                    <p><strong>Contact :</strong> %s</p>
                </body>
                </html>
                """.formatted(lignes, ADRESSE_HOTEL, CONTACT_HOTEL);

        for (String destinataire : recupererDestinatairesAdministrateurs()) {
            emailService.envoyerEmailHtml(
                    destinataire,
                    "Departs clients du jour - Alpha Hotel",
                    emailHtml
            );
        }
    }

    public DisponibiliteSearchForm buildDisponibiliteSearchForm() {
        DisponibiliteSearchForm form = new DisponibiliteSearchForm();
        form.setDateArrivee(LocalDate.now());
        form.setDateDepart(LocalDate.now().plusDays(1));
        return form;
    }

    public ByteArrayInputStream exporterReservationsCsv(String recherche, String statut, String statutSejour) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID;Client;Email;Telephone;Chambre;Type;Date arrivee;Date depart;Montant total;Acompte;Statut;Statut sejour\n");
        for (Reservation reservation : listerReservationsFiltrees(recherche, statut, statutSejour)) {
            csv.append(reservation.getId()).append(';')
                    .append(echapperCsv(reservation.getClient().getNomComplet())).append(';')
                    .append(echapperCsv(reservation.getClient().getEmail())).append(';')
                    .append(echapperCsv(reservation.getClient().getTelephone())).append(';')
                    .append(echapperCsv(reservation.getChambre().getNumero())).append(';')
                    .append(reservation.getChambre().getType()).append(';')
                    .append(reservation.getDateArrivee()).append(';')
                    .append(reservation.getDateDepart()).append(';')
                    .append(reservation.getMontantTotal()).append(';')
                    .append(reservation.getAcompte()).append(';')
                    .append(reservation.getStatut()).append(';')
                    .append(reservation.getStatutSejour()).append('\n');
        }
        return new ByteArrayInputStream(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private Client creerClient(ReservationForm form) {
        Client client = new Client();
        client.setNomComplet(form.getNomComplet());
        client.setEmail(form.getEmail());
        client.setTelephone(form.getTelephone());
        return client;
    }

    private void enregistrerAvanceClient(Reservation reservation, ReservationForm form) {
        Paiement paiement = new Paiement();
        paiement.setReservation(reservation);
        paiement.setMontant(reservation.getAcompte().setScale(2, RoundingMode.HALF_UP));
        paiement.setLibelle(form.getLibelleAvance());
        paiement.setModePaiement(form.getModePaiementAvance());
        paiement.setTelephoneMobileMoney("MOBILE_MONEY".equals(form.getModePaiementAvance()) ? form.getTelephonePaiementMobile() : null);
        paiement.setStatut(StatutPaiement.PARTIEL);
        Paiement savedPaiement = paiementRepository.save(paiement);
        reservation.getPaiements().add(savedPaiement);
    }

    private void recalculerMontants(Reservation reservation) {
        long nuits = Math.max(1, ChronoUnit.DAYS.between(reservation.getDateArrivee(), reservation.getDateDepart()));
        BigDecimal montantTotal = reservation.getChambre().getPrixParNuit()
                .multiply(BigDecimal.valueOf(nuits))
                .setScale(2, RoundingMode.HALF_UP);
        reservation.setMontantTotal(montantTotal);
        reservation.setAcompte(montantTotal.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP));
    }

    private boolean correspondRecherche(Reservation reservation, String recherche) {
        if (recherche == null || recherche.isBlank()) {
            return true;
        }
        String needle = recherche.toLowerCase();
        return String.valueOf(reservation.getId()).contains(needle)
                || reservation.getClient().getNomComplet().toLowerCase().contains(needle)
                || reservation.getClient().getEmail().toLowerCase().contains(needle)
                || reservation.getChambre().getNumero().toLowerCase().contains(needle);
    }

    private String echapperCsv(String valeur) {
        if (valeur == null) {
            return "";
        }
        return "\"" + valeur.replace("\"", "\"\"") + "\"";
    }

    private void notifierAdministrateursNouvelleReservation(Reservation reservation) {
        String emailHtml = """
                <html>
                <body style="font-family: Arial, sans-serif; color: #1f2937;">
                    <h2>Nouvelle reservation en attente - Alpha Hotel</h2>
                    <p>Une nouvelle reservation vient d'etre soumise par un client.</p>
                    <ul>
                        <li>Client : %s</li>
                        <li>Email client : %s</li>
                        <li>Telephone : %s</li>
                        <li>Chambre : %s - %s</li>
                        <li>Sejour : %s au %s</li>
                        <li>Montant total : %s Ar</li>
                        <li>Avance recue : %s Ar</li>
                        <li>Mode de paiement de l'avance : %s</li>
                        <li>%s</li>
                    </ul>
                    <p><strong>Adresse hotel :</strong> %s</p>
                    <p><strong>Contacts :</strong> %s</p>
                    <p>Merci de vous connecter a l'interface d'administration pour valider cette reservation.</p>
                    <p>Lien : <a href="http://localhost:8080/auth/login">http://localhost:8080/auth/login</a></p>
                </body>
                </html>
                """.formatted(
                reservation.getClient().getNomComplet(),
                reservation.getClient().getEmail(),
                reservation.getClient().getTelephone(),
                reservation.getChambre().getNumero(),
                reservation.getChambre().getType(),
                reservation.getDateArrivee(),
                reservation.getDateDepart(),
                reservation.getMontantTotal(),
                reservation.getAcompte(),
                reservation.getPaiements().isEmpty() ? "N/A" : reservation.getPaiements().get(0).getModePaiement(),
                reservation.getPaiements().isEmpty() || reservation.getPaiements().get(0).getTelephoneMobileMoney() == null || reservation.getPaiements().get(0).getTelephoneMobileMoney().isBlank()
                        ? "Numero Mobile Money : non renseigne"
                        : "Numero Mobile Money : " + reservation.getPaiements().get(0).getTelephoneMobileMoney(),
                ADRESSE_HOTEL,
                CONTACT_HOTEL
        );

        for (String destinataire : recupererDestinatairesAdministrateurs()) {
            emailService.envoyerEmailHtml(
                    destinataire,
                    "Nouvelle reservation client a valider - Alpha Hotel",
                    emailHtml
            );
        }
    }

    private Set<String> recupererDestinatairesAdministrateurs() {
        Set<String> destinataires = new java.util.LinkedHashSet<>();

        if (notificationAdmin != null && !notificationAdmin.isBlank()) {
            destinataires.add(notificationAdmin.trim());
        }

        utilisateurRepository.findByRoleInAndActifTrue(List.of(RoleUtilisateur.ADMIN, RoleUtilisateur.DIRECTION))
                .stream()
                .map(com.alpha.hotel.model.Utilisateur::getEmail)
                .forEach(destinataires::add);

        return destinataires;
    }
}
