package org.example;

import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.junit5.AllureJunit5;
import io.qameta.allure.selenide.AllureSelenide;
import org.example.config.BrowserConfig;
import org.example.model.TestUser;
import org.example.pages.LoginPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.Selenide.closeWebDriver;
import static com.codeborne.selenide.Selenide.open;

/**
 * Base class for all UI test classes.
 *
 * <p>Responsibilities (SRP — each concern is delegated):
 * <ul>
 *   <li>Browser lifecycle: open and close the WebDriver around each test.</li>
 *   <li>Allure integration: register/deregister the {@link AllureSelenide} listener
 *       so screenshots and page sources are attached to reports on failure.</li>
 *   <li>Shared helpers: provide {@link #loginAs(TestUser)} so subclasses never
 *       duplicate login logic (DRY).</li>
 * </ul>
 * </p>
 *
 * <p>Browser configuration is delegated to {@link BrowserConfig} (SRP).
 * Screenshot-on-failure is delegated to {@link ScreenshotWatcher} (SRP).</p>
 *
 * <p>All test classes must extend this class to inherit the setup/teardown lifecycle.</p>
 */
@ExtendWith({AllureJunit5.class, ScreenshotWatcher.class})
public class BaseTest {

    /** Allure listener key used to register and remove the listener by name. */
    private static final String ALLURE_LISTENER_KEY = "allure";

    /**
     * Runs before each test method.
     *
     * <ol>
     *   <li>Applies Chrome args conditionally via {@link BrowserConfig#applyBrowserOptions()},
     *       respecting the {@code selenide.headless} property from {@code selenide.properties}.</li>
     *   <li>Registers the {@link AllureSelenide} listener to capture screenshots and
     *       page sources on failure. {@code enableLogs()} attaches Selenide action logs
     *       to the Allure report; {@code screenshots(true)} attaches a screenshot on
     *       any test failure.</li>
     *   <li>Opens the base URL ({@code /}) as defined in {@code selenide.properties}.</li>
     * </ol>
     */
    @BeforeEach
    void setUp() {
        BrowserConfig.applyBrowserOptions();
        SelenideLogger.addListener(ALLURE_LISTENER_KEY,
                new AllureSelenide()
                        .screenshots(true)
                        .savePageSource(false));
        open("/");
    }

    /**
     * Runs after each test method.
     *
     * <p>Closes the WebDriver instance and removes the Allure listener to prevent
     * listener accumulation across tests.</p>
     */
    @AfterEach
    void tearDown() {
        closeWebDriver();
        SelenideLogger.removeListener(ALLURE_LISTENER_KEY);
    }

    /**
     * Shared login helper — eliminates duplicated login setup across all test subclasses (DRY).
     *
     * <p>Navigates to the login page (already open from {@link #setUp()}) and submits
     * the credentials associated with the given {@link TestUser}.</p>
     *
     * @param user the {@link TestUser} enum constant whose credentials to use
     */
    protected void loginAs(TestUser user) {
        new LoginPage()
                .enterUsername(user.username)
                .enterPassword(user.password)
                .submit();
    }
}
