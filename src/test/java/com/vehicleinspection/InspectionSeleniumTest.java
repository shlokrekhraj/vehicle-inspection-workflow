package com.vehicleinspection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InspectionSeleniumTest {

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");

        driver = new ChromeDriver(options);
    }

    @Test
    void shouldSubmitVehicleInspectionRequest() {
        driver.get("http://localhost:8080/inspection-test.html");

        driver.findElement(By.id("vehicleNumber")).sendKeys("MH02AB5678");
        driver.findElement(By.id("vehicleType")).sendKeys("Car");
        driver.findElement(By.id("ownerName")).sendKeys("Selenium Test User");
        driver.findElement(By.id("documentName")).sendKeys("Registration Certificate");
        driver.findElement(By.id("submitButton")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        wait.until(d -> {
            String result = d.findElement(By.id("result")).getText();
            return result.contains("HTTP Status:")
                    || result.contains("Request failed:");
        });

        String result = driver.findElement(By.id("result")).getText();

        assertTrue(result.contains("HTTP Status: 201"),
                "Expected HTTP 201 but received: " + result);
        assertTrue(result.contains("MH02AB5678"),
                "Response should contain the submitted vehicle number");
        assertTrue(result.contains("SUBMITTED"),
                "Response should contain SUBMITTED status");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
