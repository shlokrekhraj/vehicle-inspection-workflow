package com.vehicleinspection.model;

import jakarta.persistence.*;

@Entity
@Table(name = "inspection_requests")
public class InspectionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String vehicleNumber;

    @Column(nullable = false)
    private String vehicleType;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String documentName;

    @Column(nullable = false)
    private String status;

    public InspectionRequest() {
    }

    public InspectionRequest(String vehicleNumber, String vehicleType,
                             String ownerName, String documentName) {
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.ownerName = ownerName;
        this.documentName = documentName;
        this.status = "SUBMITTED";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}