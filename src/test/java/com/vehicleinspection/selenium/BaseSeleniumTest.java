package com.vehicleinspection.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Shared browser set-up and page helpers for the Selenium suite. */
@ExtendWith(ScreenshotOnFailureExtension.class)
public abstract class BaseSeleniumTest {

    protected static final String BASE_URL =
            System.getProperty("app.baseUrl", "http://localhost:8080").replaceAll("/+$", "");

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeEach
    void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (!"false".equalsIgnoreCase(System.getProperty("selenium.headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--disable-gpu", "--no-sandbox", "--window-size=1280,900");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ---------- helpers ----------

    protected void open(String page) {
        driver.get(BASE_URL + "/" + page);
    }

    protected void type(String id, String text) {
        WebElement field = driver.findElement(By.id(id));
        field.clear();
        field.sendKeys(text);
    }

    protected void choose(String id, String visibleText) {
        new Select(driver.findElement(By.id(id))).selectByVisibleText(visibleText);
    }

    protected void click(String id) {
        wait.until(ExpectedConditions.elementToBeClickable(By.id(id))).click();
    }

    /** Waits for a .msg box to appear and returns its text. */
    protected String messageOf(String id) {
        WebElement box = driver.findElement(By.id(id));
        wait.until(d -> box.getAttribute("class").contains("show"));
        return box.getText();
    }

    protected boolean isSuccess(String id) {
        return driver.findElement(By.id(id)).getAttribute("class").contains("success");
    }

    /** A valid, unique Indian registration number, e.g. MH12AB4821. */
    protected static String randomVehicleNumber() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return "MH" + (10 + r.nextInt(89)) + (char) ('A' + r.nextInt(26)) + (char) ('A' + r.nextInt(26))
                + (1000 + r.nextInt(9000));
    }

    /** Submits a valid request through apply.html and returns the new request ID. */
    protected long submitValidRequest(String vehicleNumber) {
        open("apply.html");
        type("vehicleNumber", vehicleNumber);
        choose("vehicleType", "Car");
        type("ownerName", "Selenium Test User");
        type("documentName", "Registration Certificate");
        click("submitButton");
        String text = messageOf("submitMessage");
        Matcher m = Pattern.compile("Request ID (\\d+)").matcher(text);
        if (!m.find()) {
            throw new AssertionError("Submission failed: " + text);
        }
        return Long.parseLong(m.group(1));
    }

    protected String trackStatus(long id) {
        open("status.html");
        type("requestId", String.valueOf(id));
        click("trackButton");
        return messageOf("statusMessage");
    }
}
