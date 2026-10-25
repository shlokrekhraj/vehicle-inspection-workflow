package com.vehicleinspection.service;

import com.vehicleinspection.exception.InvalidRequestException;
import com.vehicleinspection.exception.ResourceNotFoundException;
import com.vehicleinspection.model.InspectionRequest;
import com.vehicleinspection.model.InspectionStatus;
import com.vehicleinspection.repository.InspectionRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** MVP tasks 4, 7, 8, 9 and 13 - create, store, list, view and track requests. */
@Service
public class InspectionRequestService {

    private final InspectionRequestRepository repository;
    private final InspectionValidationService validationService;

    public InspectionRequestService(InspectionRequestRepository repository,
                                    InspectionValidationService validationService) {
        this.repository = repository;
        this.validationService = validationService;
    }

    public InspectionRequest submit(InspectionRequest request) {
        List<String> errors = validationService.validate(request);
        if (!errors.isEmpty()) {
            throw new InvalidRequestException(errors);
        }
        request.setId(null);
        request.setRemarks(null);
        request.setStatus(InspectionStatus.SUBMITTED);
        return repository.save(request);
    }

    public List<InspectionRequest> list(String status) {
        if (status == null || status.isBlank()) {
            return repository.findAllByOrderByIdDesc();
        }
        return repository.findByStatusOrderByIdDesc(status.trim().toUpperCase());
    }

    public InspectionRequest get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection request not found"));
    }
}
