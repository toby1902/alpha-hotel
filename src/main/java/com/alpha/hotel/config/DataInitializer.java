package com.alpha.hotel.config;

import com.alpha.hotel.model.Chambre;
import com.alpha.hotel.model.StockPPN;
import com.alpha.hotel.model.Utilisateur;
import com.alpha.hotel.model.enums.RoleUtilisateur;
import com.alpha.hotel.model.enums.TypeChambre;
import com.alpha.hotel.repository.ChambreRepository;
import com.alpha.hotel.repository.StockPPNRepository;
import com.alpha.hotel.repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final ChambreRepository chambreRepository;
    private final StockPPNRepository stockPPNRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UtilisateurRepository utilisateurRepository,
                           ChambreRepository chambreRepository,
                           StockPPNRepository stockPPNRepository,
                           PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.chambreRepository = chambreRepository;
        this.stockPPNRepository = stockPPNRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        creerUtilisateursParDefaut();
        creerChambresParDefaut();
        creerStocksParDefaut();
    }

    private void creerUtilisateursParDefaut() {
        if (utilisateurRepository.count() == 0) {
            Utilisateur admin = new Utilisateur();
            admin.setNomComplet("Administrateur Alpha Hotel");
            admin.setEmail("admin@alphahotel.com");
            admin.setMotDePasse(passwordEncoder.encode("Admin@123"));
            admin.setRole(RoleUtilisateur.ADMIN);
            admin.setActif(true);

            Utilisateur direction = new Utilisateur();
            direction.setNomComplet("Direction Alpha Hotel");
            direction.setEmail("direction@alphahotel.com");
            direction.setMotDePasse(passwordEncoder.encode("Direction@123"));
            direction.setRole(RoleUtilisateur.DIRECTION);
            direction.setActif(true);

            utilisateurRepository.saveAll(List.of(admin, direction));
        }
    }

    private void creerChambresParDefaut() {
        if (chambreRepository.count() == 0) {
            chambreRepository.saveAll(List.of(
                    new Chambre("101", TypeChambre.SIMPLE, new BigDecimal("120000"), 1, true, "Chambre simple confortable"),
                    new Chambre("201", TypeChambre.DOUBLE, new BigDecimal("180000"), 2, true, "Chambre double avec vue"),
                    new Chambre("301", TypeChambre.SUITE, new BigDecimal("350000"), 4, true, "Suite premium pour sejour luxe")
            ));
        } else {
            chambreRepository.findAll().forEach(chambre -> {
                if ("101".equals(chambre.getNumero())) {
                    chambre.setPrixParNuit(new BigDecimal("120000"));
                } else if ("201".equals(chambre.getNumero())) {
                    chambre.setPrixParNuit(new BigDecimal("180000"));
                } else if ("301".equals(chambre.getNumero())) {
                    chambre.setPrixParNuit(new BigDecimal("350000"));
                }
                chambreRepository.save(chambre);
            });
        }
    }

    private void creerStocksParDefaut() {
        if (stockPPNRepository.count() == 0) {
            stockPPNRepository.saveAll(List.of(
                    new StockPPN("Eau minerale", 25, "Bouteille", new BigDecimal("3000")),
                    new StockPPN("Savon", 4, "Piece", new BigDecimal("2500")),
                    new StockPPN("Papier toilette", 12, "Rouleau", new BigDecimal("4000"))
            ));
        }
    }
}
