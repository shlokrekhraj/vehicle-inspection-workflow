package com.vehicleinspection.repository;

import com.vehicleinspection.model.InspectionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InspectionRequestRepository extends JpaRepository<InspectionRequest, Long> {
}