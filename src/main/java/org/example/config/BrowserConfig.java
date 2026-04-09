package org.example.config;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.chrome.ChromeOptions;

import static com.codeborne.selenide.Selenide.$;

/**
 * Utility class responsible solely for configuring the Selenide/WebDriver browser
 * settings (Single Responsibility Principle).
 *
 * <p>Selenide automatically reads {@code src/test/resources/selenide.properties} at
 * startup and populates {@link Configuration} fields ({@code baseUrl}, {@code browser},
 * {@code timeout}, {@code headless}) before any test runs. This class therefore does
 * <em>not</em> duplicate those values — it only applies Chrome-specific JVM arguments
 * that cannot be expressed in the properties file.</p>
 *
 * <p>It also exposes two shared CSS selectors ({@link #CART_BADGE_SELECTOR} and
 * {@link #CART_LINK_SELECTOR}) used by both {@link org.example.pages.ProductsPage}
 * and {@link org.example.pages.CartPage}, avoiding duplication (DRY).</p>
 *
 * <p>To override headless mode at runtime without editing the properties file, pass
 * {@code -Dselenide.headless=true} as a Maven system property — Selenide picks it up
 * automatically.</p>
 */
public final class BrowserConfig {

    /**
     * CSS selector for the cart item count badge in the page header.
     * The badge is only present in the DOM when the cart contains at least one item.
     */
    public static final String CART_BADGE_SELECTOR = ".shopping_cart_badge";

    /**
     * CSS selector for the cart icon link in the page header.
     * Clicking this element navigates to {@code /cart.html}.
     */
    public static final String CART_LINK_SELECTOR = ".shopping_cart_link";

    /** Prevent instantiation — this is a static utility class. */
    private BrowserConfig() {}

    /**
     * Applies Chrome-specific JVM arguments when headless mode is active.
     *
     * <p>Selenide reads {@code selenide.properties} at startup and sets
     * {@link Configuration#headless} before this method is called. This method
     * reads that value and conditionally adds the required Chrome flags:
     * <ul>
     *   <li>{@code --headless=new} — modern headless mode (Chrome 112+)</li>
     *   <li>{@code --no-sandbox} — required in Docker and most CI environments</li>
     *   <li>{@code --disable-dev-shm-usage} — prevents shared-memory crashes
     *       in containers with limited {@code /dev/shm}</li>
     * </ul>
     * When {@code selenide.headless=false} (local development), Chrome opens with
     * a visible window and no extra arguments are applied.</p>
     */
    public static void applyBrowserOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1920,1080", "--disable-gpu");
        if (Configuration.headless) {
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        }
        Configuration.browserCapabilities = options;
    }

    /**
     * Returns {@code true} if the cart badge element is currently present in the DOM
     * and visible to the user.
     *
     * <p>Shared by {@link org.example.pages.ProductsPage} and
     * {@link org.example.pages.CartPage} to avoid duplicating the
     * exists-and-visible guard in both classes.</p>
     *
     * @return {@code true} if the badge is visible (cart has items), {@code false} otherwise
     */
    public static boolean isCartBadgeVisible() {
        SelenideElement badge = $(CART_BADGE_SELECTOR);
        return badge.exists() && badge.is(Condition.visible);
    }
}
