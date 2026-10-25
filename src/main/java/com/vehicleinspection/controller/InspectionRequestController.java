package com.vehicleinspection.controller;

import com.vehicleinspection.model.InspectionRequest;
import com.vehicleinspection.service.InspectionRequestService;
import com.vehicleinspection.service.ReviewDecisionService;
import com.vehicleinspection.service.ReviewerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inspection-requests")
public class InspectionRequestController {

    private final InspectionRequestService inspectionRequestService;
    private final ReviewerService reviewerService;
    private final ReviewDecisionService reviewDecisionService;

    public InspectionRequestController(
            InspectionRequestService inspectionRequestService,
            ReviewerService reviewerService,
            ReviewDecisionService reviewDecisionService) {

        this.inspectionRequestService = inspectionRequestService;
        this.reviewerService = reviewerService;
        this.reviewDecisionService = reviewDecisionService;
    }

    /** Applicant submits a request (tasks 3-7). */
    @PostMapping
    public ResponseEntity<InspectionRequest> submitRequest(
            @RequestBody InspectionRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inspectionRequestService.submit(request));
    }

    /** Reviewer dashboard: all requests, optionally filtered by ?status= (task 8). */
    @GetMapping
    public List<InspectionRequest> listRequests(
            @RequestParam(required = false) String status) {

        return inspectionRequestService.list(status);
    }

    /** Request details view (task 9). */
    @GetMapping("/{id}")
    public InspectionRequest getRequest(@PathVariable Long id) {
        return inspectionRequestService.get(id);
    }

    /** Reviewer picks up a request (task 10). */
    @PutMapping("/{id}/review")
    public InspectionRequest startReview(@PathVariable Long id) {
        return reviewerService.startReview(id);
    }

    /** Approve / reject with remarks (tasks 11, 12, 14). */
    @PutMapping("/{id}/decision")
    public InspectionRequest makeDecision(
            @PathVariable Long id,
            @RequestParam String decision,
            @RequestParam(required = false) String remarks) {

        return reviewDecisionService.makeDecision(id, decision, remarks);
    }

    /** Status tracking for the applicant (task 13). */
    @GetMapping("/{id}/status")
    public InspectionRequest getStatus(@PathVariable Long id) {
        return inspectionRequestService.get(id);
    }
}
