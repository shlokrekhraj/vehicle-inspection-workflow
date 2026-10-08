package com.vehicleinspection.service;

import com.vehicleinspection.model.InspectionRequest;
import org.springframework.stereotype.Service;

@Service
public class InspectionValidationService {

    public boolean validateRequest(InspectionRequest request) {

        if (request.getVehicleNumber() == null ||
            request.getVehicleNumber().isBlank()) {
            return false;
        }

        if (request.getVehicleType() == null ||
            request.getVehicleType().isBlank()) {
            return false;
        }

        if (request.getOwnerName() == null ||
            request.getOwnerName().isBlank()) {
            return false;
        }

        if (request.getDocumentName() == null ||
            request.getDocumentName().isBlank()) {
            return false;
        }

        return true;
    }
}