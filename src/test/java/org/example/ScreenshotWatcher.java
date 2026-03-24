package org.example;

import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Allure;
import org.example.util.ScreenshotNamer;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
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

import static com.codeborne.selenide.Selenide.closeWebDriver;

/**
 * JUnit 5 {@link TestWatcher} extension that captures a browser screenshot on
 * test failure and attaches it to the Allure report (Requirements 9.1, 9.2, 9.3).
 *
 * <p>Registered via {@code @ExtendWith} in {@link BaseTest} so every test subclass
 * automatically benefits without per-class configuration (DRY / OCP).</p>
 *
 * <p>This watcher also owns the WebDriver lifecycle. {@code BaseTest.tearDown()} must
 * NOT call {@code closeWebDriver()} — if it did, the driver would already be closed
 * by the time {@link #testFailed} fires (JUnit 5 runs {@code @AfterEach} before
 * {@link TestWatcher} callbacks). Instead, every callback here closes the driver
 * after it is no longer needed.</p>
 *
 * <p>Why not rely solely on {@code AllureSelenide.screenshots(true)}?
 * {@code AllureSelenide} only intercepts failures that originate from Selenide's own
 * {@code shouldBe}/{@code shouldHave} assertions. AssertJ failures ({@code assertThat})
 * bypass Selenide entirely, so no screenshot would be attached for those cases.
 * This watcher fires on <em>every</em> test failure regardless of assertion library.</p>
 */
public class ScreenshotWatcher implements TestWatcher {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotWatcher.class);

    /** Directory where screenshots are also persisted as files for CI artifact upload. */
    private static final String SCREENSHOTS_DIR = "target/screenshots";

    /**
     * Invoked by JUnit 5 immediately after a test method throws an exception.
     *
     * <p>Steps:
     * <ol>
     *   <li>Check that a WebDriver session is still active.</li>
     *   <li>Capture the viewport as a PNG byte array via the WebDriver API directly —
     *       this works for both Selenide and AssertJ failures.</li>
     *   <li>Attach the PNG to the current Allure test result inline.</li>
     *   <li>Also write the PNG to {@code target/screenshots/} for CI artifact upload.</li>
     *   <li>Close the WebDriver (driver must still be open at this point).</li>
     * </ol>
     * </p>
     *
     * @param context the JUnit 5 extension context providing test class/method metadata
     * @param cause   the throwable that caused the test to fail
     */
    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        if (!WebDriverRunner.hasWebDriverStarted()) {
            log.warn("WebDriver not active — skipping screenshot for {}",
                    context.getDisplayName());
            return;
        }

        try {
            WebDriver driver = WebDriverRunner.getWebDriver();
            byte[] screenshotBytes = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.BYTES);

            // Attach inline to Allure report
            String attachmentName = "Screenshot on failure — " + context.getDisplayName();
            try (ByteArrayInputStream stream = new ByteArrayInputStream(screenshotBytes)) {
                Allure.addAttachment(attachmentName, "image/png", stream, ".png");
            }

            // Also persist to target/screenshots/ for CI artifact upload
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
        } finally {
            closeWebDriver();
        }
    }

    /**
     * Invoked by JUnit 5 after a test completes successfully.
     * Closes the WebDriver session.
     *
     * @param context the JUnit 5 extension context
     */
    @Override
    public void testSuccessful(ExtensionContext context) {
        closeWebDriver();
    }

    /**
     * Invoked by JUnit 5 after a test is aborted (e.g. via {@code Assumptions.assumeTrue}).
     * Closes the WebDriver session.
     *
     * @param context the JUnit 5 extension context
     * @param cause   the assumption failure that caused the abort
     */
    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        closeWebDriver();
    }

    /**
     * Invoked by JUnit 5 when a test is disabled (skipped).
     * No WebDriver was started, so nothing to close.
     *
     * @param context the JUnit 5 extension context
     * @param reason  the optional reason the test was disabled
     */
    @Override
    public void testDisabled(ExtensionContext context, java.util.Optional<String> reason) {
        // No driver started for disabled tests — nothing to do
    }
}
