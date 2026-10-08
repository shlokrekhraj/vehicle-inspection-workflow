package com.vehicleinspection.service;

import com.vehicleinspection.model.InspectionRequest;
import com.vehicleinspection.repository.InspectionRequestRepository;
import org.springframework.stereotype.Service;

@Service
public class ReviewDecisionService {

    private final InspectionRequestRepository inspectionRequestRepository;

    public ReviewDecisionService(
            InspectionRequestRepository inspectionRequestRepository) {
        this.inspectionRequestRepository = inspectionRequestRepository;
    }

    public InspectionRequest makeDecision(Long requestId, String decision) {

        InspectionRequest request = inspectionRequestRepository
                .findById(requestId)
                .orElseThrow(() ->
                        new RuntimeException("Inspection request not found"));

        if (!"UNDER_REVIEW".equals(request.getStatus())) {
            throw new RuntimeException(
                    "Only requests under review can receive a decision");
        }

        if ("APPROVE".equalsIgnoreCase(decision)) {
            request.setStatus("APPROVED");

        } else if ("REJECT".equalsIgnoreCase(decision)) {
            request.setStatus("REJECTED");

        } else {
            throw new RuntimeException(
                    "Decision must be APPROVE or REJECT");
        }

        return inspectionRequestRepository.save(request);
    }
}