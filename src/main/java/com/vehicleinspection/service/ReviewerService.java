package com.vehicleinspection.service;

import com.vehicleinspection.exception.InvalidStateException;
import com.vehicleinspection.exception.ResourceNotFoundException;
import com.vehicleinspection.model.InspectionRequest;
import com.vehicleinspection.model.InspectionStatus;
import com.vehicleinspection.repository.InspectionRequestRepository;
import org.springframework.stereotype.Service;

/** MVP task 10 - Review Action. */
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
                        new ResourceNotFoundException("Inspection request not found"));

        if (!InspectionStatus.SUBMITTED.equals(request.getStatus())) {
            throw new InvalidStateException(
                    "Only submitted requests can be sent for review");
        }

        request.setStatus(InspectionStatus.UNDER_REVIEW);

        return inspectionRequestRepository.save(request);
    }
}
