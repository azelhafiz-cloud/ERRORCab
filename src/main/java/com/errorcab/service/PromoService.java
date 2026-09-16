package com.errorcab.service;

import com.errorcab.model.PromoCode;
import com.errorcab.repository.PromoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service managing promo code verification and discount calculation in Java.
 */
@Service
public class PromoService {
    private final PromoRepository promoRepo = new PromoRepository();

    public record PromoResult(boolean valid, String code, double discount, double finalFare, String message) {}

    public PromoResult applyPromo(String code, double subtotalFare) {
        if (code == null || code.trim().isEmpty()) {
            return new PromoResult(false, "", 0.0, subtotalFare, "Please enter a valid promo code.");
        }

        Optional<PromoCode> promoOpt = promoRepo.findByCode(code.trim());
        if (promoOpt.isEmpty()) {
            return new PromoResult(false, code, 0.0, subtotalFare, "Invalid or expired promo code.");
        }

        PromoCode promo = promoOpt.get();
        if (subtotalFare < promo.getMinimumFare()) {
            return new PromoResult(false, promo.getCode(), 0.0, subtotalFare,
                    String.format("Minimum fare of ₹%.0f required for code %s.", promo.getMinimumFare(), promo.getCode()));
        }

        double discount = promo.calculateDiscount(subtotalFare);
        double finalFare = Math.round(Math.max(30.0, subtotalFare - discount));

        return new PromoResult(true, promo.getCode(), discount, finalFare,
                String.format("Promo code %s applied! Saved ₹%.0f.", promo.getCode(), discount));
    }

    public List<PromoCode> getActivePromos() {
        return promoRepo.getAllActivePromos();
    }
}
