package org.example;

import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.junit5.AllureJunit5;
import io.qameta.allure.selenide.AllureSelenide;
import org.example.config.BrowserConfig;
import org.example.model.TestUser;
import org.example.pages.CartPage;
import org.example.pages.CheckoutPage;
import org.example.pages.LoginPage;
import org.example.pages.ProductDetailPage;
import org.example.pages.ProductsPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.Selenide.clearBrowserCookies;
import static com.codeborne.selenide.Selenide.clearBrowserLocalStorage;
import static com.codeborne.selenide.Selenide.closeWebDriver;
import static com.codeborne.selenide.Selenide.open;

/**
 * Base class for all UI test classes.
 *
 * <p>Responsibilities (SRP — each concern is delegated):
 * <ul>
 *   <li>Browser lifecycle: open a clean browser state before each test and close
 *       WebDriver after each test.</li>
 *   <li>Allure integration: register/deregister the {@link AllureSelenide} listener
 *       so screenshots and page sources are attached to reports on failure.</li>
 *   <li>Page object fields: pre-instantiated page objects available to all subclasses
 *       so test files contain only test logic (DRY).</li>
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

    // -------------------------------------------------------------------------
    // Pre-instantiated page objects — available to all subclasses (DRY)
    // -------------------------------------------------------------------------

    /** Page object for the login screen ({@code /}). */
    protected LoginPage loginPage;

    /** Page object for the product listing page ({@code /inventory.html}). */
    protected ProductsPage productsPage;

    /** Page object for a product detail page ({@code /inventory-item.html}). */
    protected ProductDetailPage productDetailPage;

    /** Page object for the shopping cart page ({@code /cart.html}). */
    protected CartPage cartPage;

    /** Page object for the multi-step checkout flow. */
    protected CheckoutPage checkoutPage;

    /**
     * Runs before each test method.
     *
     * <ol>
     *   <li>Applies Chrome args conditionally via {@link BrowserConfig#applyBrowserOptions()},
     *       respecting the {@code selenide.headless} property from {@code selenide.properties}.</li>
     *   <li>Registers the {@link AllureSelenide} listener to capture screenshots and
     *       page sources on failure.</li>
     *   <li>Opens the base URL ({@code /}) as defined in {@code selenide.properties}.</li>
     *   <li>Instantiates all page objects so subclasses can use them directly.</li>
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
        clearBrowserCookies();
        clearBrowserLocalStorage();
        open("/");

        loginPage       = new LoginPage();
        productsPage    = new ProductsPage();
        productDetailPage = new ProductDetailPage();
        cartPage        = new CartPage();
        checkoutPage    = new CheckoutPage();
    }

    /**
     * Runs after each test method.
     *
     * <p>Removes the Allure listener to prevent accumulation across tests, then
     * closes WebDriver to enforce test isolation.</p>
     */
    @AfterEach
    void tearDown() {
        SelenideLogger.removeListener(ALLURE_LISTENER_KEY);
        closeWebDriver();
    }

    /**
     * Shared login helper — eliminates duplicated login setup across all test subclasses (DRY).
     *
     * <p>Uses the pre-instantiated {@link #loginPage} field to submit credentials.</p>
     *
     * @param user the {@link TestUser} enum constant whose credentials to use
     */
    protected void loginAs(TestUser user) {
        loginPage.enterUsername(user.username)
                 .enterPassword(user.password)
                 .submit();
    }
}
