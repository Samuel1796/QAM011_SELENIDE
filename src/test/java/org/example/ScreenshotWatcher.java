package org.example;

import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Allure;
import org.example.util.ScreenshotNamer;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Captures a screenshot whenever a test throws, then rethrows the original failure.
 *
 * <p>This callback runs before JUnit invokes {@code @AfterEach}, so the driver is
 * still alive when the screenshot is taken. Driver shutdown remains centralized in
 * {@link BaseTest#tearDown()} for consistent test isolation.</p>
 */
public class ScreenshotWatcher implements TestExecutionExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotWatcher.class);
    private static final String SCREENSHOTS_DIR = "target/screenshots";

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        captureFailureScreenshot(context);
        throw throwable;
    }

    private void captureFailureScreenshot(ExtensionContext context) {
        if (!WebDriverRunner.hasWebDriverStarted()) {
            log.warn("WebDriver not active - skipping screenshot for {}", context.getDisplayName());
            return;
        }

        try {
            WebDriver driver = WebDriverRunner.getWebDriver();
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            String attachmentName = "Screenshot on failure - " + context.getDisplayName();
            try (ByteArrayInputStream stream = new ByteArrayInputStream(screenshotBytes)) {
                Allure.addAttachment(attachmentName, "image/png", stream, ".png");
            }

            String filename = ScreenshotNamer.buildName(
                    context.getRequiredTestClass().getSimpleName(),
                    context.getRequiredTestMethod().getName()
            );
            Path dir = Paths.get(SCREENSHOTS_DIR);
            Files.createDirectories(dir);
            Path filePath = dir.resolve(filename);
            Files.write(filePath, screenshotBytes);
            log.info("Screenshot attached to Allure and saved to: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to save screenshot to disk: {}", e.getMessage(), e);
        } catch (ClassCastException e) {
            log.warn("WebDriver does not support screenshots: {}", e.getMessage());
        }
    }
}
