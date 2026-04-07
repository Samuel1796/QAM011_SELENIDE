package org.example.util;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

/**
 * Utility class providing reusable Selenide helper methods shared across page objects.
 *
 * <p>Centralises common UI interaction patterns to avoid duplication (DRY) and keep
 * page objects focused on their own page's concerns (SRP). All methods are static
 * because they are stateless helpers with no dependency on page-specific state.</p>
 */
public final class SelenideHelper {

    /** Prevent instantiation — this is a static utility class. */
    private SelenideHelper() {}

    /**
     * Returns the visible text of an element identified by the given CSS selector,
     * or an empty string if the element does not exist or is not currently visible.
     *
     * <p>This method is used by page objects that need to read an optional UI element
     * (such as an error banner) without throwing an {@code ElementNotFound} exception
     * when the element is absent. The caller can then assert on the returned string
     * without needing to guard against null or exceptions.</p>
     *
     * <p>Example usage in a page object:
     * <pre>{@code
     * public String getErrorMessage() {
     *     return SelenideHelper.getOptionalText("[data-test='error']");
     * }
     * }</pre>
     * </p>
     *
     * @param cssSelector the CSS selector of the target element
     * @return the element's visible text, or {@code ""} if the element is absent or hidden
     */
    public static String getOptionalText(String cssSelector) {
        SelenideElement el = $(cssSelector);
        if (el.exists() && el.is(Condition.visible)) {
            return el.getText();
        }
        return "";
    }
}
