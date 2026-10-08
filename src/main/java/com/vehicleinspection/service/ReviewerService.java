package com.vehicleinspection.service;

import com.vehicleinspection.model.InspectionRequest;
import com.vehicleinspection.repository.InspectionRequestRepository;
import org.springframework.stereotype.Service;

@Service
public class ReviewerService {

    private final InspectionRequestRepository inspectionRequestRepository;

    public ReviewerService(InspectionRequestRepository inspectionRequestRepository) {
        this.inspectionRequestRepository = inspectionRequestRepository;
    }

    public InspectionRequest startReview(Long requestId) {

        InspectionRequest request = inspectionRequestRepository
                .findById(requestId)
                .orElseThrow(() ->
                        new RuntimeException("Inspection request not found"));

        if (!"SUBMITTED".equals(request.getStatus())) {
            throw new RuntimeException(
                    "Only submitted requests can be sent for review");
        }

        request.setStatus("UNDER_REVIEW");

        return inspectionRequestRepository.save(request);
    }
}