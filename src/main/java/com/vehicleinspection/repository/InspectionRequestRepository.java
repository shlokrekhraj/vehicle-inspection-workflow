package com.vehicleinspection.repository;

import com.vehicleinspection.model.InspectionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionRequestRepository extends JpaRepository<InspectionRequest, Long> {

    List<InspectionRequest> findAllByOrderByIdDesc();

    List<InspectionRequest> findByStatusOrderByIdDesc(String status);
}
