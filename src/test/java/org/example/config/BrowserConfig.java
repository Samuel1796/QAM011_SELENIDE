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
 * <p>The {@code SELENIDE_HEADLESS} environment variable (set in Docker / GitHub Actions)
 * overrides the properties file value at runtime (OCP — open for extension via env vars).</p>
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
     * <p>Selenide already sets {@link Configuration#headless} from {@code selenide.properties}
     * or the {@code SELENIDE_HEADLESS} environment variable before this method is called.
     * This method reads that value and conditionally adds the required Chrome flags:
     * <ul>
     *   <li>{@code --headless=new} — modern headless mode (Chrome 112+)</li>
     *   <li>{@code --no-sandbox} — required in Docker/CI environments</li>
     *   <li>{@code --disable-dev-shm-usage} — prevents shared memory crashes in containers</li>
     * </ul>
     * When {@code selenide.headless=false} (local development), no extra args are applied
     * and Chrome opens normally with a visible window.</p>
     */
    public static void applyBrowserOptions() {
        // Respect the headless flag already loaded from selenide.properties or env var
        boolean headless = Configuration.headless
                || Boolean.parseBoolean(System.getenv("SELENIDE_HEADLESS"));

        if (headless) {
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
