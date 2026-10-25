package com.vehicleinspection.service;

import com.vehicleinspection.model.InspectionRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/** MVP task 6 - Input Validation. */
@Service
public class InspectionValidationService {

    /** Indian registration format, e.g. MH02AB1234 or DL3C1234 (spaces/hyphens ignored). */
    private static final Pattern VEHICLE_NUMBER =
            Pattern.compile("^[A-Z]{2}[0-9]{1,2}[A-Z]{0,3}[0-9]{4}$");

    private static final Set<String> VEHICLE_TYPES =
            Set.of("CAR", "BIKE", "SCOOTER", "TRUCK", "BUS", "AUTO", "VAN");

    /** Normalises the request in place and returns every problem found (empty = valid). */
    public List<String> validate(InspectionRequest request) {
        List<String> errors = new ArrayList<>();

        if (isBlank(request.getVehicleNumber())) {
            errors.add("vehicleNumber: is required");
        } else {
            String normalised = request.getVehicleNumber()
                    .toUpperCase().replaceAll("[\\s-]", "");
            request.setVehicleNumber(normalised);
            if (!VEHICLE_NUMBER.matcher(normalised).matches()) {
                errors.add("vehicleNumber: must look like MH02AB1234");
            }
        }

        if (isBlank(request.getVehicleType())) {
            errors.add("vehicleType: is required");
        } else if (!VEHICLE_TYPES.contains(request.getVehicleType().trim().toUpperCase())) {
            errors.add("vehicleType: must be one of " + VEHICLE_TYPES);
        }

        if (isBlank(request.getOwnerName())) {
            errors.add("ownerName: is required");
        } else if (request.getOwnerName().trim().length() < 3) {
            errors.add("ownerName: must be at least 3 characters");
        }

        if (isBlank(request.getDocumentName())) {
            errors.add("documentName: is required");
        }

        return errors;
    }

    /** Kept for backwards compatibility with the first MVP version. */
    public boolean validateRequest(InspectionRequest request) {
        return validate(request).isEmpty();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
