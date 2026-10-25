package com.vehicleinspection.model;

/** Allowed values of InspectionRequest.status, in workflow order. */
public final class InspectionStatus {

    public static final String SUBMITTED = "SUBMITTED";
    public static final String UNDER_REVIEW = "UNDER_REVIEW";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";

    private InspectionStatus() {
    }
}
