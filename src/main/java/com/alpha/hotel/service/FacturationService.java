package com.alpha.hotel.service;

import com.alpha.hotel.dto.PaiementForm;
import com.alpha.hotel.model.Facture;
import com.alpha.hotel.model.Paiement;
import com.alpha.hotel.model.Reservation;
import com.alpha.hotel.model.enums.StatutFacture;
import com.alpha.hotel.model.enums.StatutPaiement;
import com.alpha.hotel.repository.FactureRepository;
import com.alpha.hotel.repository.PaiementRepository;
import com.alpha.hotel.repository.ReservationRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FacturationService {

    private final FactureRepository factureRepository;
    private final PaiementRepository paiementRepository;
    private final ReservationRepository reservationRepository;

    public FacturationService(FactureRepository factureRepository,
                              PaiementRepository paiementRepository,
                              ReservationRepository reservationRepository) {
        this.factureRepository = factureRepository;
        this.paiementRepository = paiementRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Facture creerOuMettreAJourFacture(Reservation reservation) {
        Facture facture = factureRepository.findByReservationId(reservation.getId())
                .orElseGet(() -> creerFacture(reservation));

        BigDecimal totalPaye = reservation.getPaiements().stream()
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        facture.setMontantTotal(reservation.getMontantTotal());
        facture.setMontantPaye(totalPaye);
        facture.setResteAPayer(reservation.getMontantTotal().subtract(totalPaye).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));

        if (facture.getMontantPaye().compareTo(BigDecimal.ZERO) == 0) {
            facture.setStatut(StatutFacture.BROUILLON);
        } else if (facture.getResteAPayer().compareTo(BigDecimal.ZERO) == 0) {
            facture.setStatut(StatutFacture.REGLEE);
        } else {
            facture.setStatut(StatutFacture.PARTIELLEMENT_REGLEE);
        }

        return factureRepository.save(facture);
    }

    @Transactional
    public Paiement enregistrerPaiement(Long reservationId, PaiementForm form) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation introuvable."));

        Facture facture = creerOuMettreAJourFacture(reservation);
        if (facture.getResteAPayer().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cette reservation est deja totalement reglee.");
        }
        if (form.getMontant().compareTo(facture.getResteAPayer()) > 0) {
            throw new IllegalArgumentException("Le montant saisi depasse le reste a payer de la reservation.");
        }

        Paiement paiement = new Paiement();
        paiement.setReservation(reservation);
        paiement.setMontant(form.getMontant().setScale(2, RoundingMode.HALF_UP));
        paiement.setModePaiement(form.getModePaiement());
        paiement.setLibelle((form.getLibelle() == null || form.getLibelle().isBlank()) ? "Paiement complementaire" : form.getLibelle());
        paiement.setStatut(determinerStatutPaiement(reservation, paiement.getMontant()));

        Paiement savedPaiement = paiementRepository.save(paiement);
        reservation.getPaiements().add(savedPaiement);
        creerOuMettreAJourFacture(reservation);
        return savedPaiement;
    }

    @Transactional
    public Facture recupererFactureParReservation(Long reservationId) {
        return factureRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Facture introuvable pour cette reservation."));
    }

    public BigDecimal calculerMontantTotalEncaisse() {
        return paiementRepository.findAll().stream()
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public byte[] genererFacturePdf(Long reservationId) {
        Facture facture = recupererFactureParReservation(reservationId);
        Reservation reservation = facture.getReservation();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font texte = FontFactory.getFont(FontFactory.HELVETICA, 11);

            document.add(new Paragraph("Alpha Hotel - Facture", titre));
            document.add(new Paragraph("Numero : " + facture.getNumeroFacture(), texte));
            document.add(new Paragraph("Date : " + facture.getDateEmission().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), texte));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Client : " + reservation.getClient().getNomComplet(), texte));
            document.add(new Paragraph("Email : " + reservation.getClient().getEmail(), texte));
            document.add(new Paragraph("Chambre : " + reservation.getChambre().getNumero() + " - " + reservation.getChambre().getType(), texte));
            document.add(new Paragraph("Sejour : " + reservation.getDateArrivee() + " au " + reservation.getDateDepart(), texte));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.addCell("Montant total");
            table.addCell(facture.getMontantTotal() + " Ar");
            table.addCell("Montant paye");
            table.addCell(facture.getMontantPaye() + " Ar");
            table.addCell("Reste a payer");
            table.addCell(facture.getResteAPayer() + " Ar");
            table.addCell("Statut");
            table.addCell(facture.getStatut().name());
            document.add(table);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Historique des paiements", titre));

            List<Paiement> paiements = reservation.getPaiements();
            if (paiements.isEmpty()) {
                document.add(new Paragraph("Aucun paiement enregistre.", texte));
            } else {
                PdfPTable paiementTable = new PdfPTable(4);
                paiementTable.setWidthPercentage(100);
                paiementTable.addCell("Date");
                paiementTable.addCell("Libelle");
                paiementTable.addCell("Mode");
                paiementTable.addCell("Montant");
                for (Paiement paiement : paiements) {
                    paiementTable.addCell(paiement.getDatePaiement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    paiementTable.addCell(paiement.getLibelle());
                    paiementTable.addCell(paiement.getModePaiement());
                    paiementTable.addCell(paiement.getMontant() + " Ar");
                }
                document.add(paiementTable);
            }

            document.close();
            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("Impossible de generer le PDF de la facture.", exception);
        }
    }

    private Facture creerFacture(Reservation reservation) {
        Facture facture = new Facture();
        facture.setReservation(reservation);
        facture.setNumeroFacture("FAC-" + reservation.getId() + "-" + System.currentTimeMillis());
        facture.setDateEmission(LocalDateTime.now());
        facture.setMontantTotal(reservation.getMontantTotal());
        facture.setMontantPaye(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        facture.setResteAPayer(reservation.getMontantTotal().setScale(2, RoundingMode.HALF_UP));
        facture.setStatut(StatutFacture.BROUILLON);
        return facture;
    }

    private StatutPaiement determinerStatutPaiement(Reservation reservation, BigDecimal nouveauPaiement) {
        BigDecimal dejaPaye = reservation.getPaiements().stream()
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = dejaPaye.add(nouveauPaiement);

        if (total.compareTo(reservation.getMontantTotal()) >= 0) {
            return StatutPaiement.REGLE;
        }
        return total.compareTo(BigDecimal.ZERO) > 0 ? StatutPaiement.PARTIEL : StatutPaiement.EN_ATTENTE;
    }
}
