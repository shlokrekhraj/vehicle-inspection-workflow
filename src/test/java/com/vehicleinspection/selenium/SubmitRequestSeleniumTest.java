package com.vehicleinspection.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Journeys 2 and 3 - submitting a request, and validation of bad input. */
class SubmitRequestSeleniumTest extends BaseSeleniumTest {

    @Test
    @DisplayName("J2: valid request is stored with status SUBMITTED")
    void validRequestIsSubmitted() {
        String vehicle = randomVehicleNumber();
        long id = submitValidRequest(vehicle);

        String text = messageOf("submitMessage");
        assertTrue(isSuccess("submitMessage"), text);
        assertTrue(text.contains(vehicle), text);
        assertTrue(text.contains("SUBMITTED"), text);
        assertTrue(id > 0);
    }

    @Test
    @DisplayName("J3: invalid vehicle details are rejected with clear errors")
    void invalidRequestShowsValidationErrors() {
        open("apply.html");
        type("vehicleNumber", "12345");
        type("ownerName", "Al");
        click("submitButton");

        String text = messageOf("submitMessage");
        assertFalse(isSuccess("submitMessage"), "Invalid data was accepted: " + text);
        assertTrue(text.contains("vehicleNumber: must look like MH02AB1234"), text);
        assertTrue(text.contains("vehicleType: is required"), text);
        assertTrue(text.contains("documentName: is required"), text);
    }
}
