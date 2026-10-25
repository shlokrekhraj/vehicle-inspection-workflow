package com.vehicleinspection.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.*;

/** Journeys 4 and 5 - reviewer approves / rejects, applicant tracks the result. */
class ReviewWorkflowSeleniumTest extends BaseSeleniumTest {

    private void startReview(long id) {
        open("reviewer.html");
        click("review-" + id);
        wait.until(d -> d.findElement(By.id("status-" + id)).getText().equals("UNDER_REVIEW"));
    }

    @Test
    @DisplayName("J4: reviewer approves a request and the applicant sees APPROVED")
    void reviewerApprovesRequest() {
        long id = submitValidRequest(randomVehicleNumber());
        startReview(id);

        type("remarks", "All documents verified");
        click("approve-" + id);
        wait.until(d -> d.findElement(By.id("status-" + id)).getText().equals("APPROVED"));
        assertTrue(messageOf("dashboardMessage").contains("is now APPROVED"));

        String status = trackStatus(id);
        assertTrue(status.contains("APPROVED"), status);
        assertTrue(status.contains("All documents verified"), status);
    }

    @Test
    @DisplayName("J5: rejection needs remarks, and the applicant sees them")
    void reviewerRejectsRequestWithRemarks() {
        long id = submitValidRequest(randomVehicleNumber());
        startReview(id);

        click("reject-" + id);
        String error = messageOf("dashboardMessage");
        assertFalse(isSuccess("dashboardMessage"), error);
        assertTrue(error.contains("remarks"), error);

        type("remarks", "Pollution certificate expired");
        click("reject-" + id);
        wait.until(d -> d.findElement(By.id("status-" + id)).getText().equals("REJECTED"));

        String status = trackStatus(id);
        assertTrue(status.contains("REJECTED"), status);
        assertTrue(status.contains("Pollution certificate expired"), status);
    }
}
