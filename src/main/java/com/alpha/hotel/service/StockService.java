package com.alpha.hotel.service;

import com.alpha.hotel.dto.StockForm;
import com.alpha.hotel.model.StockPPN;
import com.alpha.hotel.repository.StockPPNRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockService.class);
    private static final int SEUIL_CRITIQUE = 5;

    private final StockPPNRepository stockPPNRepository;

    public StockService(StockPPNRepository stockPPNRepository) {
        this.stockPPNRepository = stockPPNRepository;
    }

    public List<StockPPN> listerStocks() {
        return stockPPNRepository.findAll();
    }

    public List<StockPPN> recupererAlertesActives() {
        return stockPPNRepository.findByQuantiteLessThanEqualOrderByQuantiteAsc(SEUIL_CRITIQUE);
    }

    @Transactional
    public StockPPN enregistrer(StockForm stockForm) {
        StockPPN stock = new StockPPN();
        stock.setNomProduit(stockForm.getNomProduit());
        stock.setQuantite(stockForm.getQuantite());
        stock.setUnite(stockForm.getUnite());
        stock.setPrixUnitaire(stockForm.getPrixUnitaire());

        return verifierEtSauvegarder(stock);
    }

    @Transactional
    public StockPPN verifierEtSauvegarder(StockPPN stock) {
        // Une alerte systeme est levee si le stock devient critique.
        if (stock.getQuantite() <= SEUIL_CRITIQUE) {
            stock.setAlerteActive(true);
            stock.setMessageAlerte("Stock critique pour " + stock.getNomProduit() + " : notification WhatsApp a prevoir.");
            LOGGER.warn(stock.getMessageAlerte());
        } else {
            stock.setAlerteActive(false);
            stock.setMessageAlerte(null);
        }

        return stockPPNRepository.save(stock);
    }
}
