package com.errorcab.copilot.controller;

import com.errorcab.copilot.model.CopilotContext;
import com.errorcab.copilot.model.CopilotResponse;
import com.errorcab.copilot.model.CopilotTripRequest;
import com.errorcab.copilot.service.AiCopilotService;
import com.errorcab.model.User;
import com.errorcab.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for ERRORCab AI — Travel Copilot.
 * Exposes endpoints for travel plan generation, context inspection,
 * preferences schema, and system health status.
 */
@RestController
@RequestMapping("/api/ai/copilot")
public class AiCopilotController {

    private final AiCopilotService copilotService;
    private final SessionService sessionService;

    public AiCopilotController(AiCopilotService copilotService, SessionService sessionService) {
        this.copilotService = copilotService;
        this.sessionService = sessionService;
    }

    /**
     * Synthesizes a personalized travel plan with ERRORCab ride suggestions.
     * POST /api/ai/copilot/plan
     */
    @PostMapping("/plan")
    public ResponseEntity<?> generatePlan(@RequestBody(required = false) CopilotTripRequest request,
                                          HttpServletRequest httpRequest) {
        try {
            if (request == null) {
                request = new CopilotTripRequest();
            }

            // If passengerId is not supplied in body, attempt resolution via session token
            if (request.getPassengerId() <= 0) {
                Optional<SessionService.UserSession> sessionOpt = sessionService.getSessionFromRequest(httpRequest);
                if (sessionOpt.isPresent()) {
                    request.setPassengerId(sessionOpt.get().userId());
                } else {
                    // Default to passenger ID 1 for seamless local demo and direct testing
                    request.setPassengerId(1);
                }
            }

            CopilotResponse response = copilotService.generateTravelPlan(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to generate travel plan: " + e.getMessage()
            ));
        }
    }

    /**
     * Retrieves aggregated passenger context (profile snippet, favorites, past trips, fleet).
     * GET /api/ai/copilot/context?passengerId=1
     */
    @GetMapping("/context")
    public ResponseEntity<?> getContext(@RequestParam(required = false) Integer passengerId,
                                        HttpServletRequest httpRequest) {
        int pid = 1;
        if (passengerId != null && passengerId > 0) {
            pid = passengerId;
        } else {
            Optional<SessionService.UserSession> sessionOpt = sessionService.getSessionFromRequest(httpRequest);
            if (sessionOpt.isPresent()) {
                pid = sessionOpt.get().userId();
            }
        }

        CopilotContext context = copilotService.buildContext(pid);
        return ResponseEntity.ok(context);
    }

    /**
     * AI Provider health and status check.
     * GET /api/ai/copilot/status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(copilotService.getStatus());
    }

    /**
     * Returns schema and presets for preferences (purpose, destination, budget, food, interests, activities).
     * GET /api/ai/copilot/schema
     */
    @GetMapping("/schema")
    public ResponseEntity<?> getSchema() {
        return ResponseEntity.ok(copilotService.getPreferencesSchema());
    }
}
