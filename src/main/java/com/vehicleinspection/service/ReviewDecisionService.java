package com.vehicleinspection.service;

import com.vehicleinspection.exception.InvalidRequestException;
import com.vehicleinspection.exception.InvalidStateException;
import com.vehicleinspection.exception.ResourceNotFoundException;
import com.vehicleinspection.model.InspectionRequest;
import com.vehicleinspection.model.InspectionStatus;
import com.vehicleinspection.repository.InspectionRequestRepository;
import org.springframework.stereotype.Service;

/** MVP tasks 11, 12 and 14 - Approval, Rejection and Review Remarks. */
@Service
public class ReviewDecisionService {

    private final InspectionRequestRepository inspectionRequestRepository;

    public ReviewDecisionService(
            InspectionRequestRepository inspectionRequestRepository) {
        this.inspectionRequestRepository = inspectionRequestRepository;
    }

    public InspectionRequest makeDecision(Long requestId, String decision) {
        return makeDecision(requestId, decision, null);
    }

    public InspectionRequest makeDecision(Long requestId, String decision, String remarks) {

        InspectionRequest request = inspectionRequestRepository
                .findById(requestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inspection request not found"));

        if (!InspectionStatus.UNDER_REVIEW.equals(request.getStatus())) {
            throw new InvalidStateException(
                    "Only requests under review can receive a decision");
        }

        String cleanRemarks = remarks == null ? null : remarks.trim();

        if ("APPROVE".equalsIgnoreCase(decision)) {
            request.setStatus(InspectionStatus.APPROVED);

        } else if ("REJECT".equalsIgnoreCase(decision)) {
            if (cleanRemarks == null || cleanRemarks.isEmpty()) {
                throw new InvalidRequestException(
                        "remarks: are required when rejecting a request");
            }
            request.setStatus(InspectionStatus.REJECTED);

        } else {
            throw new InvalidRequestException(
                    "Decision must be APPROVE or REJECT");
        }

        if (cleanRemarks != null && cleanRemarks.length() > 500) {
            throw new InvalidRequestException("remarks: must be at most 500 characters");
        }

        request.setRemarks(cleanRemarks == null || cleanRemarks.isEmpty() ? null : cleanRemarks);

        return inspectionRequestRepository.save(request);
    }
}
