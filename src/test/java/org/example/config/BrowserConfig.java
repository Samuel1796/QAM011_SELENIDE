package org.example.config;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.chrome.ChromeOptions;

import static com.codeborne.selenide.Selenide.$;

/**
 * Responsible solely for configuring the Selenide/WebDriver browser settings (SRP).
 *
 * <p>Selenide automatically reads {@code src/test/resources/selenide.properties} at startup
 * and populates {@link Configuration} fields ({@code baseUrl}, {@code browser},
 * {@code timeout}, {@code headless}) before any test runs. This class therefore does
 * <em>not</em> duplicate those values — it only applies Chrome-specific JVM arguments
 * that cannot be expressed in the properties file.</p>
 *
 * <p>Properties consumed from {@code selenide.properties}:
 * <ul>
 *   <li>{@code selenide.baseUrl} — used by {@code open("/")} in {@link org.example.BaseTest}</li>
 *   <li>{@code selenide.browser} — selects the WebDriver implementation</li>
 *   <li>{@code selenide.timeout} — default element wait timeout in milliseconds</li>
 *   <li>{@code selenide.headless} — when {@code true}, headless Chrome args are applied here</li>
 * </ul>
 * </p>
 *
 * <p>To override headless mode at runtime (CI / Docker) without editing the properties file,
 * pass {@code -Dselenide.headless=true} as a Maven system property. Selenide picks this up
 * automatically and sets {@link Configuration#headless} before any test runs.</p>
 */
public final class BrowserConfig {

    /** CSS selector for the cart item count badge in the page header. */
    public static final String CART_BADGE_SELECTOR = ".shopping_cart_badge";

    /** CSS selector for the cart icon link in the page header. */
    public static final String CART_LINK_SELECTOR = ".shopping_cart_link";

    /** Prevent instantiation of this utility class. */
    private BrowserConfig() {}

    /**
     * Applies Chrome-specific JVM arguments when headless mode is active.
     *
     * <p>Selenide reads {@code selenide.properties} automatically at startup and sets
     * {@link Configuration#headless} before this method is called. This method simply
     * reads that value and conditionally adds the required Chrome flags:
     * <ul>
     *   <li>{@code --headless=new} — modern headless mode (Chrome 112+)</li>
     *   <li>{@code --no-sandbox} — required in Docker/CI environments</li>
     *   <li>{@code --disable-dev-shm-usage} — prevents shared memory crashes in containers</li>
     * </ul>
     * To run headless in CI or Docker without editing the properties file, pass
     * {@code -Dselenide.headless=true} as a Maven system property — Selenide picks it up
     * automatically. When {@code selenide.headless=false} (local development), Chrome opens
     * with a visible window and no extra args are applied.</p>
     */
    public static void applyBrowserOptions() {
        // Configuration.headless is already set by Selenide from selenide.properties
        // or the -Dselenide.headless system property — no env var needed.
        if (Configuration.headless) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
            Configuration.browserCapabilities = options;
        }
    }

    /**
     * Returns {@code true} if the cart badge element is currently visible in the header.
     *
     * <p>Shared by {@link org.example.pages.ProductsPage} and {@link org.example.pages.CartPage}
     * to avoid duplicating the exists-and-visible guard (DRY).</p>
     *
     * @return {@code true} if the badge is visible (cart has items), {@code false} otherwise
     */
    public static boolean isCartBadgeVisible() {
        SelenideElement badge = $(CART_BADGE_SELECTOR);
        return badge.exists() && badge.is(Condition.visible);
    }
}
