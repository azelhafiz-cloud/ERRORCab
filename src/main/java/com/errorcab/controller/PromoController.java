package com.errorcab.controller;

import com.errorcab.model.PromoCode;
import com.errorcab.service.PromoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Promo Codes and Discount Calculations.
 */
@RestController
@RequestMapping({"/api/promo", "/api/promos"})
public class PromoController {
    private final PromoService promoService;

    public PromoController(PromoService promoService) {
        this.promoService = promoService;
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validatePromo(
            @RequestParam String code,
            @RequestParam double amount) {
        var res = promoService.applyPromo(code, amount);
        return ResponseEntity.ok(Map.of(
                "valid", res.valid(),
                "code", res.code(),
                "discount", res.discount(),
                "finalFare", res.finalFare(),
                "message", res.message()
        ));
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyPromo(@RequestBody Map<String, Object> body) {
        try {
            String code = body.get("code").toString();
            double subtotal = Double.parseDouble(body.get("subtotal").toString());
            var result = promoService.applyPromo(code, subtotal);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<PromoCode>> getPromos() {
        return ResponseEntity.ok(promoService.getActivePromos());
    }
}
