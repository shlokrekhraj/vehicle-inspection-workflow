package com.vehicleinspection.controller;

import com.vehicleinspection.model.InspectionRequest;
import com.vehicleinspection.repository.InspectionRequestRepository;
import com.vehicleinspection.service.InspectionValidationService;
import com.vehicleinspection.service.ReviewerService;
import com.vehicleinspection.service.ReviewDecisionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inspection-requests")
public class InspectionRequestController {

    private final InspectionRequestRepository inspectionRequestRepository;
    private final InspectionValidationService validationService;
    private final ReviewerService reviewerService;
    private final ReviewDecisionService reviewDecisionService;

    public InspectionRequestController(
            InspectionRequestRepository inspectionRequestRepository,
            InspectionValidationService validationService,
            ReviewerService reviewerService,
            ReviewDecisionService reviewDecisionService) {

        this.inspectionRequestRepository = inspectionRequestRepository;
        this.validationService = validationService;
        this.reviewerService = reviewerService;
        this.reviewDecisionService = reviewDecisionService;
    }

    @PostMapping
    public ResponseEntity<?> submitRequest(
            @RequestBody InspectionRequest request) {

        if (!validationService.validateRequest(request)) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid inspection request data");
        }

        request.setStatus("SUBMITTED");

        InspectionRequest savedRequest =
                inspectionRequestRepository.save(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedRequest);
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<?> startReview(@PathVariable Long id) {

        try {
            InspectionRequest request = reviewerService.startReview(id);
            return ResponseEntity.ok(request);

        } catch (RuntimeException exception) {
            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    @PutMapping("/{id}/decision")
    public ResponseEntity<?> makeDecision(
            @PathVariable Long id,
            @RequestParam String decision) {

        try {
            InspectionRequest request =
                    reviewDecisionService.makeDecision(id, decision);

            return ResponseEntity.ok(request);

        } catch (RuntimeException exception) {
            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<?> getStatus(@PathVariable Long id) {

    InspectionRequest request =
            inspectionRequestRepository.findById(id).orElse(null);

    if (request == null) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Inspection request not found");
    }

    return ResponseEntity.ok(request);
}
}