package com.vehicleinspection.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Journey 1 - an applicant registers and logs in. */
class UserAccountSeleniumTest extends BaseSeleniumTest {

    @Test
    @DisplayName("J1: applicant can register and then log in")
    void registerThenLogin() {
        String email = "applicant" + System.currentTimeMillis() + "@test.com";
        open("index.html");

        type("regName", "Selenium Applicant");
        type("regEmail", email);
        type("regPassword", "secret123");
        click("registerButton");
        String registered = messageOf("registerMessage");
        assertTrue(isSuccess("registerMessage"), "Registration failed: " + registered);
        assertTrue(registered.contains(email), registered);
        assertTrue(registered.contains("APPLICANT"), registered);

        type("loginEmail", email);
        type("loginPassword", "secret123");
        click("loginButton");
        String login = messageOf("loginMessage");
        assertTrue(isSuccess("loginMessage"), "Login failed: " + login);
        assertTrue(login.contains("Welcome, Selenium Applicant"), login);
    }

    @Test
    @DisplayName("J1b: wrong password is rejected")
    void wrongPasswordIsRejected() {
        open("index.html");
        type("loginEmail", "nobody" + System.currentTimeMillis() + "@test.com");
        type("loginPassword", "not-the-password");
        click("loginButton");
        String login = messageOf("loginMessage");
        assertFalse(isSuccess("loginMessage"));
        assertTrue(login.contains("Invalid email or password"), login);
    }
}
