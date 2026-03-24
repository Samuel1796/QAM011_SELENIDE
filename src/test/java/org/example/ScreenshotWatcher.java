package org.example;

import com.codeborne.selenide.Selenide;
import org.example.util.ScreenshotNamer;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * JUnit 5 {@link TestWatcher} extension that captures a browser screenshot
 * whenever a test fails (Requirement 9.1, 9.2, 9.3).
 *
 * <p>Registered via {@code @ExtendWith} in {@link BaseTest} so every test subclass
 * automatically benefits without any per-class configuration (DRY / OCP).</p>
 *
 * <p>Screenshot naming is delegated to {@link ScreenshotNamer} (SRP — this class
 * only handles the capture and persistence concern).</p>
 *
 * <p>The {@code AllureSelenide} listener registered in {@link BaseTest} also attaches
 * screenshots to the Allure report. This watcher additionally persists them to
 * {@code target/screenshots/} for direct file access.</p>
 */
public class ScreenshotWatcher implements TestWatcher {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotWatcher.class);

    /** Directory where screenshots are saved, relative to the Maven project root. */
    private static final String SCREENSHOTS_DIR = "target/screenshots";

    /**
     * Invoked by JUnit 5 after a test method throws an exception.
     *
     * <p>Steps performed:
     * <ol>
     *   <li>Build a unique filename via {@link ScreenshotNamer#buildName(String, String)}.</li>
     *   <li>Ensure {@code target/screenshots/} exists, creating it if necessary.</li>
     *   <li>Delegate to {@link Selenide#screenshot(String)} which captures the current
     *       browser viewport and saves it as a PNG. Selenide appends {@code .png}
     *       automatically, so the extension is stripped from the path argument.</li>
     *   <li>Log the saved path for CI artifact traceability.</li>
     * </ol>
     * </p>
     *
     * @param context the JUnit 5 extension context providing test class and method metadata
     * @param cause   the throwable that caused the test to fail
     */
    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        String filename = ScreenshotNamer.buildName(
                context.getRequiredTestClass().getSimpleName(),
                context.getRequiredTestMethod().getName()
        );
        // Selenide appends ".png" automatically — pass the path without the extension
        String filenameWithoutExtension = filename.replace(".png", "");

        File screenshotsDir = new File(SCREENSHOTS_DIR);
        if (!screenshotsDir.exists()) {
            screenshotsDir.mkdirs();
        }

        String screenshotPath = Selenide.screenshot(SCREENSHOTS_DIR + "/" + filenameWithoutExtension);
        log.info("Screenshot saved: {}", screenshotPath);
    }
}
