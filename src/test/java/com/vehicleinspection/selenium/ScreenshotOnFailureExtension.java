package com.vehicleinspection.selenium;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Failure screenshot mechanism: when a test fails, saves a PNG screenshot and the
 * page HTML to target/selenium-screenshots/ BEFORE @AfterEach closes the browser.
 * Jenkins archives that folder.
 */
public class ScreenshotOnFailureExtension implements AfterTestExecutionCallback {

    static final Path OUTPUT_DIR = Paths.get("target", "selenium-screenshots");

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            return;
        }
        Object instance = context.getRequiredTestInstance();
        if (!(instance instanceof BaseSeleniumTest test) || test.driver == null) {
            return;
        }
        WebDriver driver = test.driver;
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String name = context.getRequiredTestClass().getSimpleName() + "_"
                + context.getRequiredTestMethod().getName() + "_" + stamp;
        try {
            Files.createDirectories(OUTPUT_DIR);
            Path png = OUTPUT_DIR.resolve(name + ".png");
            Path src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath();
            Files.copy(src, png, StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(OUTPUT_DIR.resolve(name + ".html"), driver.getPageSource(), StandardCharsets.UTF_8);
            System.out.println("[selenium] Test failed - screenshot saved to " + png.toAbsolutePath());
        } catch (IOException | RuntimeException e) {
            System.out.println("[selenium] Could not save failure screenshot: " + e.getMessage());
        }
    }
}
